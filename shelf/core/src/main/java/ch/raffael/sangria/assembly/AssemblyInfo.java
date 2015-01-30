package ch.raffael.sangria.assembly;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.raffael.sangria.libs.guava.io.Resources;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.sangria.annotations.bindings.AppInfo;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class AssemblyInfo {

    private static final Logger log = LoggerFactory.getLogger(AssemblyInfo.class);
    private final String id;
    private final String shortName;
    private final String fullName;
    private final String version;

    private final String vendor;
    private final String url;
    private final String support;

    private final String copyright;
    private final String shortLicense;
    private final String fullLicense;

    private final String credits;

    private AssemblyInfo(Builder builder) {
        id = builder.id;
        shortName = first(builder.shortName, builder.id);
        fullName = first(builder.fullName, builder.shortName, builder.id);
        version = builder.version;
        vendor = builder.vendor;
        url = builder.url;
        support = builder.support;
        copyright = builder.copyright;
        shortLicense = first(builder.shortLicense, builder.fullLicense);
        fullLicense = first(builder.fullLicense, builder.shortLicense);
        credits = builder.credits;
    }

    @SafeVarargs
    private static <T> T first(T... values) {
        for ( T value : values ) {
            if ( value != null ) {
                return value;
            }
        }
        return null;
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, shortName, fullName, vendor, url, support, copyright, shortLicense, fullLicense, credits);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null || getClass() != obj.getClass() ) {
            return false;
        }
        final AssemblyInfo other = (AssemblyInfo)obj;
        return Objects.equals(this.id, other.id) && Objects.equals(this.shortName, other.shortName) && Objects.equals(this.fullName, other.fullName) && Objects.equals(this.vendor, other.vendor) && Objects.equals(this.url, other.url) && Objects.equals(this.support, other.support) && Objects.equals(this.copyright, other.copyright) && Objects.equals(this.shortLicense, other.shortLicense) && Objects.equals(this.fullLicense, other.fullLicense) && Objects.equals(this.credits, other.credits);
    }

    public String getId() {
        return id;
    }

    public String getShortName() {
        return shortName;
    }

    public String getFullName() {
        return fullName;
    }

    @Nullable
    public String getVendor() {
        return vendor;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @Nullable
    public String getSupport() {
        return support;
    }

    @Nullable
    public String getCopyright() {
        return copyright;
    }

    @Nullable
    public String getShortLicense() {
        return shortLicense;
    }

    @Nullable
    public String getFullLicense() {
        return fullLicense;
    }

    @Nullable
    public String getCredits() {
        return credits;
    }

    @Nullable
    public String get(@NotNull AppInfo.Element element) {
        switch ( element ) {
            case ID:
                return id;
            case SHORT_NAME:
                return shortName;
            case FULL_NAME:
                return fullName;
            case VERSION:
                return version;
            case VENDOR:
                return vendor;
            case URL:
                return url;
            case SUPPORT:
                return support;
            case COPYRIGHT:
                return copyright;
            case SHORT_LICENSE:
                return shortLicense;
            case FULL_LICENSE:
                return fullLicense;
            case CREDITS:
                return credits;
            default:
                return null;
        }
    }

    public static final class Builder {

        private final String id;
        private String shortName;
        private String fullName;
        private String version;
        private String vendor;
        private String url;
        private String support;
        private String copyright;
        private String shortLicense;
        private String fullLicense;
        private String credits;

        private Builder(String id) {
            Validators.checkQualifiedIdentifier(id);
            this.id = id;
        }

        public Builder shortName(@Nullable String shortName) {
            this.shortName = shortName;
            return this;
        }

        public Builder fullName(@Nullable String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder version(@Nullable String version) {
            this.version = version;
            return this;
        }

        public Builder vendor(@Nullable String vendor) {
            this.vendor = vendor;
            return this;
        }

        public Builder url(@Nullable String url) {
            this.url = url;
            return this;
        }

        public Builder support(@Nullable String support) {
            this.support = support;
            return this;
        }

        public Builder copyright(@Nullable String copyright) {
            this.copyright = copyright;
            return this;
        }

        public Builder shortLicense(@Nullable String shortLicense) {
            this.shortLicense = shortLicense;
            return this;
        }

        public Builder shortLicense(@Nullable URL url) {
            return shortLicense(url, null);
        }

        public Builder shortLicense(@Nullable URL url, @Nullable Charset charset) {
            this.shortLicense = read("shortLicense", url, charset);
            return this;
        }

        public Builder fullLicense(@Nullable String fullLicense) {
            this.fullLicense = fullLicense;
            return this;
        }

        public Builder fullLicense(@Nullable URL url) {
            return fullLicense(url, null);
        }

        public Builder fullLicense(@Nullable URL url, @Nullable Charset charset) {
            this.fullLicense = read("fullLicense", url, charset);
            return this;
        }

        public Builder credits(@Nullable String credits) {
            this.credits = credits;
            return this;
        }

        public Builder credits(@Nullable URL url) {
            return credits(url, null);
        }

        public Builder credits(@Nullable URL url, @Nullable Charset charset) {
            this.credits = read("credits", url, charset);
            return this;
        }

        private static String read(@NotNull String descr, @Nullable URL url, @Nullable Charset charset) {
            if ( url == null ) {
                return null;
            }
            try {
                return Resources.toString(url, first(charset, StandardCharsets.UTF_8));
            }
            catch ( IOException e ) {
                log.error("Error reading {} from {}", descr, url, e);
                return null;
            }
        }

        public AssemblyInfo build() {
            return new AssemblyInfo(this);
        }
    }
}
