import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.artifacts.component.ModuleComponentIdentifier

gradle.projectsEvaluated {
    rootProject.tasks.register("dumpResolvedGav") {
        doLast {
            val seen = sortedSetOf<String>()
            allprojects {
                project.configurations
                    .filter { it.isCanBeResolved }
                    .forEach { configuration ->
                        configuration.incoming.resolutionResult.allComponents.forEach { component ->
                            val id = component.id
                            if (id is ModuleComponentIdentifier) {
                                seen += "${id.group}:${id.module}:${id.version}"
                            }
                        }
                    }
            }
            seen.forEach(::println)
        }
    }
}

gradle.projectsEvaluated {
    rootProject.tasks.register("dumpDirectGav") {
        doLast {
            val seen = sortedSetOf<String>()
            allprojects {
                project.configurations
                    .filter { it.isCanBeResolved }
                    .forEach { configuration ->
                        configuration.allDependencies
                            .withType(ExternalDependency::class.java)
                            .forEach { dependency ->
                                val version = dependency.version
                                if (!dependency.group.isNullOrBlank() &&
                                    !dependency.name.isNullOrBlank() &&
                                    !version.isNullOrBlank()
                                ) {
                                    seen += "${dependency.group}:${dependency.name}:$version"
                                }
                            }
                    }
            }
            seen.forEach(::println)
        }
    }
}

gradle.projectsEvaluated {
    rootProject.tasks.register("dumpResolvedGavNoTest") {
        doLast {
            val seen = sortedSetOf<String>()
            val excludedConfigPatterns = listOf(
                "test",
                "androidtest",
                "unittest",
                "testfixtures"
            )
            allprojects {
                project.configurations
                    .filter { it.isCanBeResolved }
                    .filterNot { configuration ->
                        val name = configuration.name.lowercase()
                        excludedConfigPatterns.any { it in name }
                    }
                    .forEach { configuration ->
                        configuration.incoming.resolutionResult.allComponents.forEach { component ->
                            val id = component.id
                            if (id is ModuleComponentIdentifier) {
                                seen += "${id.group}:${id.module}:${id.version}"
                            }
                        }
                    }
            }
            seen.forEach(::println)
        }
    }
}
