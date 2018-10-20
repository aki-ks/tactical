package me.aki.tactical.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Definition of a module
 */
public class Module {
    /**
     * Name of the module.
     */
    private Path module;

    /**
     * Access flags of the module.
     */
    private Set<Flag> accessFlags;

    /**
     * Version of the module.
     */
    private Optional<String> version;

    /**
     * Name of the main class in the module.
     */
    private Optional<Path> mainClass;

    /**
     * Packages of the module.
     */
    private List<Path> packages;

    /**
     * Modules that this module required/depends on.
     */
    private List<Require> requires;

    /**
     * Packages that the module exports.
     */
    private List<Export> exports;

    /**
     * Packages that the module opens for reflection.
     */
    private List<Open> opens;

    /**
     * List of service interfaces used by this module.
     */
    private List<Path> uses;

    /**
     * List of service interface implementations
     */
    private List<Provide> provides;

    public Module(Path module) {
        this(module, new HashSet<>(), Optional.empty(), Optional.empty(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>());
    }

    public Module(Path module, Set<Flag> accessFlags, Optional<String> version,
                  Optional<Path> mainClass, List<Path> packages, List<Require> requires,
                  List<Export> exports, List<Open> opens, List<Path> uses, List<Provide> provides) {
        this.module = module;
        this.accessFlags = accessFlags;
        this.version = version;
        this.mainClass = mainClass;
        this.packages = packages;
        this.requires = requires;
        this.exports = exports;
        this.opens = opens;
        this.uses = uses;
        this.provides = provides;
    }

    public Path getModule() {
        return module;
    }

    public void setModule(Path module) {
        this.module = module;
    }

    public Set<Flag> getAccessFlags() {
        return accessFlags;
    }

    public void setAccessFlags(Set<Flag> accessFlags) {
        this.accessFlags = accessFlags;
    }

    public Optional<String> getVersion() {
        return version;
    }

    public void setVersion(Optional<String> version) {
        this.version = version;
    }

    public Optional<Path> getMainClass() {
        return mainClass;
    }

    public void setMainClass(Optional<Path> mainClass) {
        this.mainClass = mainClass;
    }

    public List<Path> getPackages() {
        return packages;
    }

    public void setPackages(List<Path> packages) {
        this.packages = packages;
    }

    public List<Require> getRequires() {
        return requires;
    }

    public void setRequires(List<Require> requires) {
        this.requires = requires;
    }

    public List<Export> getExports() {
        return exports;
    }

    public void setExports(List<Export> exports) {
        this.exports = exports;
    }

    public List<Open> getOpens() {
        return opens;
    }

    public void setOpens(List<Open> opens) {
        this.opens = opens;
    }

    public List<Path> getUses() {
        return uses;
    }

    public void setUses(List<Path> uses) {
        this.uses = uses;
    }

    public List<Provide> getProvides() {
        return provides;
    }

    public void setProvides(List<Provide> provides) {
        this.provides = provides;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Module module1 = (Module) o;
        return Objects.equals(module, module1.module) &&
                Objects.equals(accessFlags, module1.accessFlags) &&
                Objects.equals(version, module1.version) &&
                Objects.equals(mainClass, module1.mainClass) &&
                Objects.equals(packages, module1.packages) &&
                Objects.equals(requires, module1.requires) &&
                Objects.equals(exports, module1.exports) &&
                Objects.equals(opens, module1.opens) &&
                Objects.equals(uses, module1.uses) &&
                Objects.equals(provides, module1.provides);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module, accessFlags, version, mainClass, packages, requires, exports, opens, uses, provides);
    }

    @Override
    public String toString() {
        return Module.class.getSimpleName() + '{' +
                "module=" + module +
                ", accessFlags=" + accessFlags +
                ", version=" + version +
                ", mainClass=" + mainClass +
                ", packages=" + packages +
                ", requires=" + requires +
                ", exports=" + exports +
                ", opens=" + opens +
                ", uses=" + uses +
                ", provides=" + provides +
                '}';
    }

    public static enum Flag {
        OPEN, SYNTHETIC, MANDATED
    }

    /**
     * A dependency of a module
     */
    public static class Require {
        /**
         * Package and name of the module
         */
        private Path name;

        /**
         * Additional flags of the required module
         */
        private Set<Flag> flags;

        /**
         * Version of the required module used during compilation
         */
        private Optional<String> version;

        public Require(Path name, Set<Flag> flags, Optional<String> version) {
            this.name = name;
            this.flags = flags;
            this.version = version;
        }

        public Path getName() {
            return name;
        }

        public void setName(Path name) {
            this.name = name;
        }

        public Set<Flag> getFlags() {
            return flags;
        }

        public void setFlags(Set<Flag> flags) {
            this.flags = flags;
        }

        public Optional<String> getVersion() {
            return version;
        }

        public void setVersion(Optional<String> version) {
            this.version = version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Require require = (Require) o;
            return Objects.equals(name, require.name) &&
                    Objects.equals(flags, require.flags) &&
                    Objects.equals(version, require.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, flags, version);
        }

        @Override
        public String toString() {
            return Require.class.getSimpleName() + '{' +
                    "name=" + name +
                    ", flags=" + flags +
                    ", version=" + version +
                    '}';
        }

        public static enum Flag {
            TRANSITIVE, STATIC_PHASE, SYNTHETIC, MANDATED
        }
    }

    /**
     * A package exported by the module
     */
    public static class Export {
        /**
         * Name of exported package.
         */
        private Path name;

        /**
         * Additional flags of the export.
         */
        private Set<Flag> flags;

        /**
         * The package is only exported to these modules.
         */
        private List<Path> modules;

        public Export(Path name, Set<Flag> flags, List<Path> modules) {
            this.name = name;
            this.flags = flags;
            this.modules = modules;
        }

        public Path getName() {
            return name;
        }

        public void setName(Path name) {
            this.name = name;
        }

        public Set<Flag> getFlags() {
            return flags;
        }

        public void setFlags(Set<Flag> flags) {
            this.flags = flags;
        }

        public List<Path> getModules() {
            return modules;
        }

        public void setModules(List<Path> modules) {
            this.modules = modules;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Export export = (Export) o;
            return Objects.equals(name, export.name) &&
                    Objects.equals(flags, export.flags) &&
                    Objects.equals(modules, export.modules);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, flags, modules);
        }

        @Override
        public String toString() {
            return Export.class.getSimpleName() + '{' +
                    "name=" + name +
                    ", flags=" + flags +
                    ", modules=" + modules +
                    '}';
        }

        public static enum Flag {
            SYNTHETIC, MANDATED
        }
    }

    /**
     * Open a package for reflection.
     */
    public static class Open {
        /**
         * Name of the exported package.
         */
        private Path name;

        /**
         * Additional flags for the "opens" expression.
         */
        private Set<Flag> flags;

        /**
         * The package is opened for these modules.
         */
        private List<Path> modules;

        public Open(Path name, Set<Flag> flags, List<Path> modules) {
            this.name = name;
            this.flags = flags;
            this.modules = modules;
        }

        public Path getName() {
            return name;
        }

        public void setName(Path name) {
            this.name = name;
        }

        public Set<Flag> getFlags() {
            return flags;
        }

        public void setFlags(Set<Flag> flags) {
            this.flags = flags;
        }

        public List<Path> getModules() {
            return modules;
        }

        public void setModules(List<Path> modules) {
            this.modules = modules;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Open opens = (Open) o;
            return Objects.equals(name, opens.name) &&
                    Objects.equals(flags, opens.flags) &&
                    Objects.equals(modules, opens.modules);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, flags, modules);
        }

        @Override
        public String toString() {
            return Open.class.getSimpleName() + '{' +
                    "name=" + name +
                    ", flags=" + flags +
                    ", modules=" + modules +
                    '}';
        }

        public static enum Flag {
            SYNTHETIC, MANDATED
        }
    }

    /**
     * Implementation of a service interface
     */
    public static class Provide {
        /**
         * Name of the implemented service interface.
         */
        private Path service;

        /**
         * List of classes implementing/providing the service.
         */
        private List<Path> providers;

        public Provide(Path service, List<Path> providers) {
            this.service = service;
            this.providers = providers;
        }

        public Path getService() {
            return service;
        }

        public void setService(Path service) {
            this.service = service;
        }

        public List<Path> getProviders() {
            return providers;
        }

        public void setProviders(List<Path> providers) {
            this.providers = providers;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Provide provide = (Provide) o;
            return Objects.equals(service, provide.service) &&
                    Objects.equals(providers, provide.providers);
        }

        @Override
        public int hashCode() {
            return Objects.hash(service, providers);
        }

        @Override
        public String toString() {
            return Provide.class.getSimpleName() + '{' +
                    "service=" + service +
                    ", providers=" + providers +
                    '}';
        }
    }
}
