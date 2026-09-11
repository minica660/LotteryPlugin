package com.example.minicash.lottery;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

public class MPluginLoader implements PluginLoader {
    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {

        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addRepository(new RemoteRepository.Builder(
                MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR,
                "default",
                "https://repo.papermc.io/repository/maven-public/"
        ).build());

        resolver.addRepository(new RemoteRepository.Builder(
                "jitpack", "default", "https://jitpack.io"
        ).build());

        resolver.addDependency(new Dependency(new DefaultArtifact("com.zaxxer:HikariCP:7.1.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.github.minica660.MiniCashLibrary:common:1.1.2"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.github.minica660.MiniCashLibrary:paper:1.1.2"), null));

        classpathBuilder.addLibrary(resolver);


    }
}
