package io.divolte.server.filesinks.gcs.entities;

import com.fasterxml.jackson.annotation.JsonCreator;

public class GcsObjectResponse {
    public final String contentType;
    public final String crc32c;
    public final String etag;
    public final String generation;
    public final String id;
    public final String kind;
    public final String md5Hash;
    public final String name;
    public final String selfLink;
    public final String size;
    public final String storageClass;
    public final String timeCreated;
    public final String timeStorageClassUpdated;

    @JsonCreator
    public GcsObjectResponse(final String contentType, final String crc32c, final String etag, final String generation, final String id, final String kind, final String md5Hash, final String name,
            final String selfLink, final String size, final String storageClass, final String timeCreated, final String timeStorageClassUpdated) {
        this.contentType = contentType;
        this.crc32c = crc32c;
        this.etag = etag;
        this.generation = generation;
        this.id = id;
        this.kind = kind;
        this.md5Hash = md5Hash;
        this.name = name;
        this.selfLink = selfLink;
        this.size = size;
        this.storageClass = storageClass;
        this.timeCreated = timeCreated;
        this.timeStorageClassUpdated = timeStorageClassUpdated;
    }

    @Override
    public String toString() {
        return "GCS File [content type=" + contentType + ", crc32c=" + crc32c + ", etag=" + etag + ", generation=" + generation + ", id=" + id
                + ", kind=" + kind + ", md5 hash=" + md5Hash + ", name=" + name + ", self link=" + selfLink + ", size=" + size + ", storage class="
                + storageClass + ", time created=" + timeCreated + ", time storage class updated=" + timeStorageClassUpdated + "]";
    }
}