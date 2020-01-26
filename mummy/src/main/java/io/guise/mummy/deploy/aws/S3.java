/*
 * Copyright © 2019 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.mummy.deploy.aws;

import static com.globalmentor.java.CharSequences.*;
import static com.globalmentor.java.Conditions.*;
import static io.guise.mummy.GuiseMummy.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static java.util.stream.StreamSupport.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

import javax.annotation.*;

import com.globalmentor.collections.*;
import com.globalmentor.net.*;
import com.globalmentor.text.StringTemplate;

import io.clogr.Clogged;
import io.confound.config.Configuration;
import io.confound.config.ConfigurationException;
import io.guise.mummy.*;
import io.guise.mummy.deploy.DeployTarget;
import io.urf.vocab.content.Content;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.*;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.model.*;

/**
 * Deploys a site to <a href="https://aws.amazon.com/s3/">AWS S3</a>.
 * @author Garret Wilson
 */
public class S3 implements DeployTarget, Clogged {

	/** The section relative key for the indication of bucket region in the configuration. */
	public static final String CONFIG_KEY_REGION = "region";

	/**
	 * The section relative key for the bucket name in the configuration; defaults to the root-relative form of {@link GuiseMummy#CONFIG_KEY_SITE_DOMAIN} resolved
	 * against any {@link GuiseMummy#CONFIG_KEY_DOMAIN} in the global configuration, falling back to {@link GuiseMummy#CONFIG_KEY_DOMAIN} if that is not present..
	 */
	public static final String CONFIG_KEY_BUCKET = "bucket";

	//# policies
	//TODO rewrite policy code using real JSON serialization

	/** The core content of a policy statement for setting a resource to public read access. No resource or condition is indicated. */
	protected static final String POLICY_STATEMENT_CONTENT_PUBLIC_READ_GET_OBJECT = //@formatter:off
			"\"Sid\":\"PublicReadGetObject\"," + 
			"\"Effect\":\"Allow\"," + 
			"\"Principal\":\"*\"," + 
			"\"Action\":[\"s3:GetObject\"],";	//@formatter:on

	/**
	 * The policy template for setting a bucket to public read access. There is one parameter:
	 * <ol>
	 * <li>S3 bucket</li>
	 * </ol>
	 */
	protected static final StringTemplate POLICY_TEMPLATE_PUBLIC_READ_GET_OBJECT = StringTemplate.builder().text( //@formatter:off
			"{" + 
			"\"Version\":\"2012-10-17\"," + 
			"\"Statement\":[{" + 
			POLICY_STATEMENT_CONTENT_PUBLIC_READ_GET_OBJECT +
			"\"Resource\":[\"arn:aws:s3:::").parameter(StringTemplate.STRING_PARAMETER).text("/*\"]" + 
			"}]" + 
			"}").build();	//@formatter:on

	/**
	 * Generates a policy with public read and get access for objects in a bucket.
	 * @implSpec This method includes {@value #POLICY_STATEMENT_CONTENT_PUBLIC_READ_GET_OBJECT} and adds no whitespace.
	 * @param bucket The S3 bucket the policy is for.
	 * @return A policy allowing public read and get access for objects in the indicated bucket.
	 */
	public static String policyPublicReadGetForBucket(@Nonnull final String bucket) {
		return POLICY_TEMPLATE_PUBLIC_READ_GET_OBJECT.apply(bucket);
	}

	/**
	 * The policy template for setting a bucket to public read access. There are two parameters:
	 * <ol>
	 * <li>S3 bucket</li>
	 * <li>IAM JSON policy condition value.</li>
	 * </ol>
	 * @see <a href="https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_condition.html">IAM JSON Policy Elements: Condition</a>
	 * @see <a href="https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_condition_operators.html">IAM JSON Policy Elements: Condition
	 *      Operators</a>
	 * @see <a href="https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_multi-value-conditions.html">Creating a Condition with Multiple Keys or
	 *      Values</a>
	 */
	protected static final StringTemplate POLICY_TEMPLATE_CONDITIONAL_PUBLIC_READ_GET_OBJECT = StringTemplate.builder().text( //@formatter:off
			"{" + 
			"\"Version\":\"2012-10-17\"," + 
			"\"Statement\":[{" + 
			POLICY_STATEMENT_CONTENT_PUBLIC_READ_GET_OBJECT +
			"\"Resource\":[\"arn:aws:s3:::").parameter(StringTemplate.STRING_PARAMETER).text("/*\"]," + 
			"\"Condition\":").parameter(StringTemplate.STRING_PARAMETER).text("" + 
			"}]" + 
			"}").build();	//@formatter:on

	/**
	 * Generates a policy with public read and get access for objects in a bucket, with a policy condition requiring the <code>aws:UserAgent</code> condition key
	 * to equal one of the provided user agents.
	 * @implSpec This method includes {@value #POLICY_STATEMENT_CONTENT_PUBLIC_READ_GET_OBJECT}. The user agents will be serialized as a JSON array in the
	 *           iteration order of the given iterable. No whitespace will be added.
	 * @param bucket The S3 bucket the policy is for.
	 * @param userAgents The user agents, any of which to accept.
	 * @return A policy allowing public read and get access for objects in the indicated bucket, restricted to the given user agents.
	 * @see <a href="https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_condition.html">IAM JSON Policy Elements: Condition</a>
	 * @see <a href="https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_elements_condition_operators.html">IAM JSON Policy Elements: Condition
	 *      Operators</a>
	 * @see <a href="https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_multi-value-conditions.html">Creating a Condition with Multiple Keys or
	 *      Values</a>
	 * @see <a href="https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_condition-keys.html#condition-keys-useragent">AWS Global Condition
	 *      Context Keys: aws:UserAgent</a>
	 */
	public static String policyPublicReadGetForBucketRequiringAnyUserAgentOf(@Nonnull final String bucket, @Nonnull final Iterable<String> userAgents) {
		return POLICY_TEMPLATE_CONDITIONAL_PUBLIC_READ_GET_OBJECT.apply(bucket, policyConditionRequiringAnyUserAgentOf(userAgents));
	}

	/**
	 * The policy condition clause template for setting a bucket to public read access. There is one parameter:
	 * <ol>
	 * <li>The values of a JSON array of user agent identification strings (e.g. <code>"foo", "bar"</code>), without the array surrounding brackets.</li>
	 * </ol>
	 */
	protected static final StringTemplate POLICY_HEADER_CONDITION_CLAUSE_TEMPLATE_PUBLIC_READ_GET_OBJECT = StringTemplate.builder()
			.text("{\"StringEquals\":{\"aws:UserAgent\":[").parameter(StringTemplate.STRING_PARAMETER).text("]}}").build();

	/**
	 * Generates a policy condition value requiring the <code>aws:UserAgent</code> condition key to equal one of the provided user agents.
	 * @implSpec The user agents will be serialized as a JSON array in the iteration order of the given iterable. No whitespace will be added.
	 * @param userAgents The user agents, any of which to accept.
	 * @return A policy condition value restricting the user agent to one of those specified.
	 * @see <a href="https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_policies_condition-keys.html#condition-keys-useragent">AWS Global Condition
	 *      Context Keys: aws:UserAgent</a>
	 * @throws IllegalArgumentException if one of the user agent strings contains the quote <code>'"'</code> character.
	 */
	public static String policyConditionRequiringAnyUserAgentOf(@Nonnull final Iterable<String> userAgents) {
		final String userAgentJsonArrayValues = stream(userAgents.spliterator(), false).map(userAgent -> {
			checkArgument(!contains(userAgent, '"'), "User agent `%s` cannot contain a quote `\"` character.", userAgent);
			return userAgent;
		}).map(userAgent -> "\"" + userAgent + "\"").collect(joining(","));
		return POLICY_HEADER_CONDITION_CLAUSE_TEMPLATE_PUBLIC_READ_GET_OBJECT.apply(userAgentJsonArrayValues);
	}

	private final String profile;

	/** @return The AWS profile if one was set explicitly. */
	public final Optional<String> getProfile() {
		return Optional.ofNullable(profile);
	}

	private final Region region;

	/** @return The specified region for deployment. */
	public Region getRegion() {
		return region;
	}

	private final String bucket;

	/** @return The destination S3 bucket for deployment. */
	public String getBucket() {
		return bucket;
	}

	/** @return A stream of all bucket names, starting with the primary bucket {@link #getBucket()}. */
	public Stream<String> buckets() {
		return Stream.of(getBucket());
	}

	private final DomainName siteDomain;

	/** @return The explicit site domain if specified, which may be the same or different than the bucket name. */
	public Optional<DomainName> getSiteDomain() {
		return Optional.ofNullable(siteDomain);
	}

	private final S3Client s3Client;

	/** @return The client for connecting to S3. */
	protected S3Client getS3Client() {
		return s3Client;
	}

	private final ReverseMap<Artifact, String> artifactKeys = new DecoratorReverseMap<>(new LinkedHashMap<>(), new HashMap<>());

	/** @return The map of S3 object keys for each artifact. */
	protected ReverseMap<Artifact, String> getArtifactKeys() {
		return artifactKeys;
	}

	/**
	 * Configuration constructor.
	 * <p>
	 * The region is retrieved from {@value #CONFIG_KEY_REGION} in the local configuration. The bucket name is retrieved from {@value #CONFIG_KEY_BUCKET} in the
	 * local configuration, falling back to {@value GuiseMummy#CONFIG_KEY_SITE_DOMAIN} and finally {@value GuiseMummy#CONFIG_KEY_DOMAIN} in the context
	 * configuration if not specified. The site domain if explicitly set is retrieved from {@value GuiseMummy#CONFIG_KEY_SITE_DOMAIN}, resolved as appropriate to
	 * any project domain.
	 * </p>
	 * @implSpec This method calls {@link #getConfiguredBucket(Configuration, Configuration)} to determine the bucket.
	 * @implSpec This method calls {@link GuiseMummy#findConfiguredSiteDomain(Configuration)} to determine the site domain, if any.
	 * @param context The context of static site generation.
	 * @param localConfiguration The local configuration for this deployment target, which may be a section of the project configuration.
	 * @see AWS#CONFIG_KEY_DEPLOY_AWS_PROFILE
	 * @see #CONFIG_KEY_REGION
	 * @see #CONFIG_KEY_BUCKET
	 * @see GuiseMummy#CONFIG_KEY_SITE_DOMAIN
	 * @see GuiseMummy#CONFIG_KEY_DOMAIN
	 * @see #getConfiguredBucket(Configuration, Configuration)
	 */
	public S3(@Nonnull final MummyContext context, @Nonnull final Configuration localConfiguration) {
		this(context.getConfiguration().findString(AWS.CONFIG_KEY_DEPLOY_AWS_PROFILE).orElse(null), Region.of(localConfiguration.getString(CONFIG_KEY_REGION)),
				getConfiguredBucket(context.getConfiguration(), localConfiguration), findConfiguredSiteDomain(context.getConfiguration()).orElse(null));
	}

	/**
	 * Region and bucket constructor.
	 * @param profile The name of the AWS profile to use for retrieving credentials, or <code>null</code> if the default credential provider should be used.
	 * @param region The AWS region of deployment.
	 * @param bucket The bucket into which the site should be deployed.
	 * @param siteDomain The full-qualified domain name of the site. If specified, it will be used in cases that which a site other than the bucket is to be
	 *          indicated, such as in redirect hostname, to prevent e.g. a CloudFront distribution redirecting back to the bucket URL.
	 * @throws IllegalArgumentException if the given site domain is not absolute.
	 */
	public S3(@Nullable String profile, @Nonnull final Region region, @Nonnull String bucket, @Nullable DomainName siteDomain) {
		this.profile = profile;
		this.region = requireNonNull(region);
		this.bucket = requireNonNull(bucket);
		if(siteDomain != null) {
			siteDomain.checkArgumentAbsolute();
		}
		this.siteDomain = siteDomain;
		final S3ClientBuilder s3ClientBuilder = S3Client.builder().region(region);
		if(profile != null) {
			s3ClientBuilder.credentialsProvider(ProfileCredentialsProvider.create(profile));
		}
		s3Client = s3ClientBuilder.build();
	}

	/**
	 * Determines the bucket to use. This method determines the bucket in the following order:
	 * <ol>
	 * <li>The key {@link #CONFIG_KEY_BUCKET} relative to the S3 configuration.</li>
	 * <li>The site domain {@value GuiseMummy#CONFIG_KEY_SITE_DOMAIN}, falling back to {@value GuiseMummy#CONFIG_KEY_DOMAIN}, relative to the domain name root
	 * (i.e. with the ending delimiter removed), retrieved from the global configuration.</li>
	 * </ol>
	 * @implSpec This method calls {@link GuiseMummy#findConfiguredSiteDomain(Configuration)}.
	 * @param globalConfiguration The configuration containing all the configuration values.
	 * @param localConfiguration The local configuration for S3, which may be a section of the project configuration.
	 * @return The configured bucket name.
	 * @see #CONFIG_KEY_BUCKET
	 * @see GuiseMummy#CONFIG_KEY_DOMAIN
	 * @see GuiseMummy#CONFIG_KEY_SITE_DOMAIN
	 * @throws ConfigurationException if the bucket name cannot be determined.
	 */
	protected static String getConfiguredBucket(@Nonnull final Configuration globalConfiguration, @Nonnull final Configuration localConfiguration)
			throws ConfigurationException {
		return localConfiguration.findString(CONFIG_KEY_BUCKET)
				.or(() -> findConfiguredSiteDomain(globalConfiguration).map(DomainName.ROOT::relativize).map(DomainName::toString))
				.orElseThrow(() -> new ConfigurationException("No configured S3 bucket could be determined."));
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation creates and configures the specified bucket as needed, calling {@link #setBucketPolicy(MummyContext, String, boolean)}.
	 */
	@Override
	public void prepare(final MummyContext context) throws IOException {
		getProfile().ifPresent(profile -> getLogger().info("Using AWS S3 credentials profile `{}`.", profile));
		final S3Client s3Client = getS3Client();
		try {
			//create bucket
			final String bucket = getBucket();
			final Region region = getRegion();
			getLogger().info("Preparing AWS S3 bucket `{}` in region `{}` for deployment.", bucket, region);
			final boolean bucketExists = bucketExists(bucket);
			getLogger().debug("Bucket `{}` exists? {}.", bucket, bucketExists);
			final boolean bucketHasPolicy;
			if(bucketExists) { //if the bucket exists, see if it has a policy
				bucketHasPolicy = bucketHasPolicy(bucket);
			} else { //create the bucket if it doesn't exist
				getLogger().info("Creating S3 bucket `{}` in AWS region `{}`.", bucket, region);
				s3Client.createBucket(request -> request.bucket(bucket).createBucketConfiguration(config -> config.locationConstraint(region.id())));
				bucketHasPolicy = false; //the bucket doesn't have a policy yet, as we just created it
			}
			//set bucket policy
			setBucketPolicy(context, bucket, bucketHasPolicy); //set the bucket policy if needed
		} catch(final SdkException sdkException) {
			throw new IOException(sdkException);
		}
	}

	/**
	 * Sets the policy of a bucket if and as appropriate.
	 * @implSpec This version does nothing.
	 * @param context The context of static site generation.
	 * @param bucket The bucket the policy of which to be set.
	 * @param hasPolicy Whether the bucket already has a policy set.
	 * @throws IOException if there is an error setting the bucket policy.
	 */
	protected void setBucketPolicy(@Nonnull final MummyContext context, @Nonnull String bucket, final boolean hasPolicy) throws IOException {
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation calls {@link #plan(MummyContext, Artifact)}, {@link #put(MummyContext)}, and {@link #prune(MummyContext)}, in that order.
	 * @return no URL, as a basic S3 deployment does not have a web site configured.
	 */
	@Override
	public Optional<URI> deploy(@Nonnull final MummyContext context, @Nonnull Artifact rootArtifact) throws IOException {
		getLogger().info("Deploying to AWS region `{}` S3 bucket `{}`.", getRegion(), getBucket());

		//#plan
		plan(context, rootArtifact);

		//#put
		put(context);

		//#prune
		prune(context);

		return Optional.empty();
	}

	/**
	 * Plans deployment of a site.
	 * @param context The context of static site generation.
	 * @param rootArtifact The root artifact of the site being deployed.
	 * @throws IOException if there is an I/O error during site deployment planning.
	 */
	protected void plan(@Nonnull final MummyContext context, @Nonnull Artifact rootArtifact) throws IOException {
		plan(context, rootArtifact, rootArtifact);
	}

	/**
	 * Plans deployment of a site for an artifact and its comprised artifacts.
	 * @implSpec For each artifact this method calls {@link #planResource(MummyContext, Artifact, Artifact, URIPath)}.
	 * @param context The context of static site generation.
	 * @param rootArtifact The root artifact of the site being deployed.
	 * @param artifact The current artifact for which deployment is being planned.
	 * @throws IOException if there is an I/O error during site deployment planning.
	 */
	protected void plan(@Nonnull final MummyContext context, @Nonnull Artifact rootArtifact, @Nonnull Artifact artifact) throws IOException {
		final URIPath resourceReference = context.relativizeTargetReference(rootArtifact, artifact);
		planResource(context, rootArtifact, artifact, resourceReference);
		if(artifact instanceof CompositeArtifact) {
			for(final Artifact comprisedArtifact : (Iterable<Artifact>)((CompositeArtifact)artifact).comprisedArtifacts()::iterator) {
				plan(context, rootArtifact, comprisedArtifact);
			}
		}
	}

	/**
	 * Plans deployment of a single resource.
	 * @implSpec This version stores an S3 key for the resource in {@link #getArtifactKeys()}.
	 * @param context The context of static site generation.
	 * @param rootArtifact The root artifact of the site being deployed.
	 * @param artifact The current artifact for which deployment is being planned.
	 * @param resourceReference A URI reference to the resource, relative to the site root.
	 * @throws IOException if there is an I/O error during site deployment planning.
	 */
	protected void planResource(@Nonnull final MummyContext context, @Nonnull Artifact rootArtifact, @Nonnull Artifact artifact,
			@Nonnull final URIPath resourceReference) throws IOException {
		final String key = resourceReference.toString();
		getLogger().debug("Planning deployment for artifact {}, S3 key `{}`.", artifact, key);
		getArtifactKeys().put(artifact, key);
	}

	/**
	 * Transfers content for deployment.
	 * @implSpec This implementation skips directories.
	 * @apiNote This is the main deployment method, which actually deploys content.
	 * @param context The context of static site generation.
	 * @throws IOException if there is an I/O error during putting.
	 */
	protected void put(@Nonnull final MummyContext context) throws IOException {
		try {
			final S3Client s3Client = getS3Client();
			final String bucket = getBucket();
			for(final Map.Entry<Artifact, String> artifactKeyEntry : getArtifactKeys().entrySet()) {
				final Artifact artifact = artifactKeyEntry.getKey();
				if(!(artifact instanceof DirectoryArtifact)) { //don't deploy anything for directories TODO improve semantics, especially after the root artifact type changes; maybe use CollectionArtifact
					final String key = artifactKeyEntry.getValue();
					getLogger().info("Deploying artifact to S3 key `{}`.", key);
					final PutObjectRequest.Builder putBuilder = PutObjectRequest.builder().bucket(bucket).key(key);
					//set content-type if found
					Content.findContentType(artifact.getResourceDescription()).map(ContentType::toString).ifPresent(putBuilder::contentType);
					s3Client.putObject(putBuilder.build(), RequestBody.fromFile(artifact.getTargetPath()));
				}
			}
		} catch(final SdkException sdkException) {
			throw new IOException(sdkException);
		}
	}

	/**
	 * Prunes any objects that don't exist in the site.
	 * @apiNote This process can occur even when actual putting is being performed concurrently, as existing objects that are in the site are left undisturbed.
	 *          There is no need to determine if the existing object is out of date, as it will be replaced if it hasn't been already. Only files no longer in the
	 *          site are removed.
	 * @param context The context of static site generation.
	 * @throws IOException if there is an I/O error during pruning.
	 */
	protected void prune(@Nonnull final MummyContext context) throws IOException {
		try {
			final S3Client s3Client = getS3Client();
			final String bucket = getBucket();
			final ReverseMap<Artifact, String> artifactKeys = getArtifactKeys();
			ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder().bucket(bucket).build();
			ListObjectsV2Response listObjectsResponse;
			do {
				listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);
				for(final S3Object s3Object : listObjectsResponse.contents()) {
					final String key = s3Object.key();
					if(!artifactKeys.containsValue(key)) { //if this object isn't in our site, delete it
						getLogger().info("Pruning S3 object `{}`.", key);
						s3Client.deleteObject(builder -> builder.bucket(bucket).key(key));
					}
				}
				listObjectsRequest = ListObjectsV2Request.builder().bucket(bucket).continuationToken(listObjectsResponse.nextContinuationToken()).build();
			} while(listObjectsResponse.isTruncated());
		} catch(final SdkException sdkException) {
			throw new IOException(sdkException);
		}
	}

	//# S3 utility methods; could be removed to separate library

	//## hosted zones

	/**
	 * Determines whether the bucket exists.
	 * @param bucket The bucket to check.
	 * @return <code>true</code> if the bucket exists; otherwise <code>false</code>.
	 * @throws SdkException if some error occurred, such as insufficient permissions.
	 */
	protected boolean bucketExists(@Nonnull final String bucket) throws SdkException {
		try {
			getS3Client().headBucket(builder -> builder.bucket(bucket));
			return true;
		} catch(final NoSuchBucketException noSuchBucketException) {
			return false;
		}
	}

	/**
	 * Determines whether a bucket has a policy.
	 * @param bucket The bucket to check.
	 * @return <code>true</code> if the bucket has a policy; otherwise <code>false</code>.
	 * @throws SdkException if some error occurred, such as insufficient permissions.
	 */
	protected boolean bucketHasPolicy(@Nonnull final String bucket) throws SdkException {
		try {
			getS3Client().getBucketPolicy(builder -> builder.bucket(bucket));
			return true;
		} catch(final S3Exception s3Exception) {
			if(s3Exception.statusCode() == HTTP.SC_NOT_FOUND) {
				return false;
			}
			throw s3Exception;
		}
	}

	/**
	 * Determines whether a bucket has a web site configuration.
	 * @param bucket The bucket to check.
	 * @return <code>true</code> if the bucket has a web site configuration; otherwise <code>false</code>.
	 * @throws SdkException if some error occurred, such as insufficient permissions.
	 */
	protected boolean bucketHasWebsiteConfiguration(@Nonnull final String bucket) throws SdkException {
		try {
			getS3Client().getBucketWebsite(builder -> builder.bucket(bucket));
			return true;
		} catch(final S3Exception s3Exception) {
			if(s3Exception.statusCode() == HTTP.SC_NOT_FOUND) {
				return false;
			}
			throw s3Exception;
		}
	}

}