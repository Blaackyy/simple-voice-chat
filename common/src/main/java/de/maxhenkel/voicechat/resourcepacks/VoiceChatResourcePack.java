package de.maxhenkel.voicechat.resourcepacks;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import de.maxhenkel.voicechat.Voicechat;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class VoiceChatResourcePack extends AbstractPackResources {

    protected String path;
    protected Component name;

    public VoiceChatResourcePack(String path, Component name) {
        super(null);
        this.path = path;
        this.name = name;
    }

    @Nullable
    public Pack toPack() {
        try {
            PackMetadataSection packMetadataSection = getMetadataSection(PackMetadataSection.SERIALIZER);
            if (packMetadataSection == null) {
                return null;
            }
            return new Pack(path, false, () -> this, name, packMetadataSection.getDescription(), PackCompatibility.forMetadata(packMetadataSection, PackType.CLIENT_RESOURCES), Pack.Position.TOP, false, PackSource.BUILT_IN);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return path;
    }

    private String getPath() {
        return "/packs/" + path + "/";
    }

    @Nullable
    private InputStream get(String name) {
        return Voicechat.class.getResourceAsStream(getPath() + name);
    }

    @Override
    protected InputStream getResource(String name) throws IOException {
        InputStream resourceAsStream = get(name);
        if (resourceAsStream == null) {
            throw new FileNotFoundException("Resource " + name + " does not exist");
        }
        return resourceAsStream;
    }

    @Override
    protected boolean hasResource(String name) {
        try {
            return get(name) != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType type, String namespace, String prefix, Predicate<ResourceLocation> pathFilter) {
        try {
            URL url = Voicechat.class.getResource(getPath());
            Path resPath = Paths.get(url.toURI());
            List<Path> files = Files.walk(resPath).collect(Collectors.toList());

            List<ResourceLocation> list = Lists.newArrayList();
            String absolutePath = type.getDirectory() + "/" + namespace + "/";
            String absolutePrefixPath = absolutePath + prefix + "/";

            for (Path path : files) {
                if (!Files.isDirectory(path)) {
                    String name = path.getFileName().toString();
                    if (!name.endsWith(".mcmeta") && name.startsWith(absolutePrefixPath)) {
                        list.add(new ResourceLocation(namespace, name.substring(absolutePath.length())));
                    }
                }
            }

            return list;

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        if (packType == PackType.CLIENT_RESOURCES) {
            return ImmutableSet.of(Voicechat.MODID);
        }
        return ImmutableSet.of();
    }

    @Override
    public void close() {

    }
}
