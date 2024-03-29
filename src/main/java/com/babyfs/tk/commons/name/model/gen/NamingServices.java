// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: meta/naming_services.proto

package com.babyfs.tk.commons.name.model.gen;

import com.google.protobuf.AbstractMessage;

public final class NamingServices {
  private NamingServices() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface NSServerOrBuilder
      extends com.google.protobuf.MessageOrBuilder {
    
    // required string id = 1;
    boolean hasId();
    String getId();
    
    // required string ip = 2;
    boolean hasIp();
    String getIp();
    
    // required int32 port = 3;
    boolean hasPort();
    int getPort();
    
    // repeated string services = 4;
    java.util.List<String> getServicesList();
    int getServicesCount();
    String getServices(int index);
    
    // optional string registerToken = 5;
    boolean hasRegisterToken();
    String getRegisterToken();
  }
  public static final class NSServer extends
      com.google.protobuf.GeneratedMessage
      implements NSServerOrBuilder {
    // Use NSServer.newBuilder() to construct.
    private NSServer(Builder builder) {
      super(builder);
    }
    private NSServer(boolean noInit) {}
    
    private static final NSServer defaultInstance;
    public static NSServer getDefaultInstance() {
      return defaultInstance;
    }
    
    public NSServer getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return NamingServices.internal_static_NSServer_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return NamingServices.internal_static_NSServer_fieldAccessorTable;
    }
    
    private int bitField0_;
    // required string id = 1;
    public static final int ID_FIELD_NUMBER = 1;
    private Object id_;
    public boolean hasId() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    public String getId() {
      Object ref = id_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (com.google.protobuf.Internal.isValidUtf8(bs)) {
          id_ = s;
        }
        return s;
      }
    }
    private com.google.protobuf.ByteString getIdBytes() {
      Object ref = id_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8((String) ref);
        id_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    
    // required string ip = 2;
    public static final int IP_FIELD_NUMBER = 2;
    private Object ip_;
    public boolean hasIp() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    public String getIp() {
      Object ref = ip_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (com.google.protobuf.Internal.isValidUtf8(bs)) {
          ip_ = s;
        }
        return s;
      }
    }
    private com.google.protobuf.ByteString getIpBytes() {
      Object ref = ip_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8((String) ref);
        ip_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    
    // required int32 port = 3;
    public static final int PORT_FIELD_NUMBER = 3;
    private int port_;
    public boolean hasPort() {
      return ((bitField0_ & 0x00000004) == 0x00000004);
    }
    public int getPort() {
      return port_;
    }
    
    // repeated string services = 4;
    public static final int SERVICES_FIELD_NUMBER = 4;
    private com.google.protobuf.LazyStringList services_;
    public java.util.List<String>
        getServicesList() {
      return services_;
    }
    public int getServicesCount() {
      return services_.size();
    }
    public String getServices(int index) {
      return services_.get(index);
    }
    
    // optional string registerToken = 5;
    public static final int REGISTERTOKEN_FIELD_NUMBER = 5;
    private Object registerToken_;
    public boolean hasRegisterToken() {
      return ((bitField0_ & 0x00000008) == 0x00000008);
    }
    public String getRegisterToken() {
      Object ref = registerToken_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (com.google.protobuf.Internal.isValidUtf8(bs)) {
          registerToken_ = s;
        }
        return s;
      }
    }
    private com.google.protobuf.ByteString getRegisterTokenBytes() {
      Object ref = registerToken_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8((String) ref);
        registerToken_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    
    private void initFields() {
      id_ = "";
      ip_ = "";
      port_ = 0;
      services_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      registerToken_ = "";
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;
      
      if (!hasId()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasIp()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasPort()) {
        memoizedIsInitialized = 0;
        return false;
      }
      memoizedIsInitialized = 1;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeBytes(1, getIdBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeBytes(2, getIpBytes());
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        output.writeInt32(3, port_);
      }
      for (int i = 0; i < services_.size(); i++) {
        output.writeBytes(4, services_.getByteString(i));
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        output.writeBytes(5, getRegisterTokenBytes());
      }
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(1, getIdBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(2, getIpBytes());
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(3, port_);
      }
      {
        int dataSize = 0;
        for (int i = 0; i < services_.size(); i++) {
          dataSize += com.google.protobuf.CodedOutputStream
            .computeBytesSizeNoTag(services_.getByteString(i));
        }
        size += dataSize;
        size += 1 * getServicesList().size();
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(5, getRegisterTokenBytes());
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }
    
    private static final long serialVersionUID = 0L;
    @Override
    protected Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }
    
    public static NSServer parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static NSServer parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static NSServer parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static NSServer parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static NSServer parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static NSServer parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static NSServer parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static NSServer parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static NSServer parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static NSServer parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(NSServer prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    @Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements NSServerOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return NamingServices.internal_static_NSServer_descriptor;
      }
      
      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return NamingServices.internal_static_NSServer_fieldAccessorTable;
      }
      
      // Construct using NamingServices.NSServer.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }
      
      private Builder(BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }
      
      public Builder clear() {
        super.clear();
        id_ = "";
        bitField0_ = (bitField0_ & ~0x00000001);
        ip_ = "";
        bitField0_ = (bitField0_ & ~0x00000002);
        port_ = 0;
        bitField0_ = (bitField0_ & ~0x00000004);
        services_ = com.google.protobuf.LazyStringArrayList.EMPTY;
        bitField0_ = (bitField0_ & ~0x00000008);
        registerToken_ = "";
        bitField0_ = (bitField0_ & ~0x00000010);
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return NSServer.getDescriptor();
      }
      
      public NSServer getDefaultInstanceForType() {
        return NSServer.getDefaultInstance();
      }
      
      public NSServer build() {
        NSServer result = buildPartial();
        if (!result.isInitialized()) {
          throw AbstractMessage.Builder.newUninitializedMessageException(result);
        }
        return result;
      }
      
      private NSServer buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        NSServer result = buildPartial();
        if (!result.isInitialized()) {
          throw AbstractMessage.Builder.newUninitializedMessageException(
                  result).asInvalidProtocolBufferException();
        }
        return result;
      }
      
      public NSServer buildPartial() {
        NSServer result = new NSServer(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.id_ = id_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.ip_ = ip_;
        if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
          to_bitField0_ |= 0x00000004;
        }
        result.port_ = port_;
        if (((bitField0_ & 0x00000008) == 0x00000008)) {
          services_ = new com.google.protobuf.UnmodifiableLazyStringList(
              services_);
          bitField0_ = (bitField0_ & ~0x00000008);
        }
        result.services_ = services_;
        if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
          to_bitField0_ |= 0x00000008;
        }
        result.registerToken_ = registerToken_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof NSServer) {
          return mergeFrom((NSServer)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(NSServer other) {
        if (other == NSServer.getDefaultInstance()) return this;
        if (other.hasId()) {
          setId(other.getId());
        }
        if (other.hasIp()) {
          setIp(other.getIp());
        }
        if (other.hasPort()) {
          setPort(other.getPort());
        }
        if (!other.services_.isEmpty()) {
          if (services_.isEmpty()) {
            services_ = other.services_;
            bitField0_ = (bitField0_ & ~0x00000008);
          } else {
            ensureServicesIsMutable();
            services_.addAll(other.services_);
          }
          onChanged();
        }
        if (other.hasRegisterToken()) {
          setRegisterToken(other.getRegisterToken());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }
      
      public final boolean isInitialized() {
        if (!hasId()) {
          
          return false;
        }
        if (!hasIp()) {
          
          return false;
        }
        if (!hasPort()) {
          
          return false;
        }
        return true;
      }
      
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder(
            this.getUnknownFields());
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              this.setUnknownFields(unknownFields.build());
              onChanged();
              return this;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                this.setUnknownFields(unknownFields.build());
                onChanged();
                return this;
              }
              break;
            }
            case 10: {
              bitField0_ |= 0x00000001;
              id_ = input.readBytes();
              break;
            }
            case 18: {
              bitField0_ |= 0x00000002;
              ip_ = input.readBytes();
              break;
            }
            case 24: {
              bitField0_ |= 0x00000004;
              port_ = input.readInt32();
              break;
            }
            case 34: {
              ensureServicesIsMutable();
              services_.add(input.readBytes());
              break;
            }
            case 42: {
              bitField0_ |= 0x00000010;
              registerToken_ = input.readBytes();
              break;
            }
          }
        }
      }
      
      private int bitField0_;
      
      // required string id = 1;
      private Object id_ = "";
      public boolean hasId() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      public String getId() {
        Object ref = id_;
        if (!(ref instanceof String)) {
          String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
          id_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      public Builder setId(String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        id_ = value;
        onChanged();
        return this;
      }
      public Builder clearId() {
        bitField0_ = (bitField0_ & ~0x00000001);
        id_ = getDefaultInstance().getId();
        onChanged();
        return this;
      }
      void setId(com.google.protobuf.ByteString value) {
        bitField0_ |= 0x00000001;
        id_ = value;
        onChanged();
      }
      
      // required string ip = 2;
      private Object ip_ = "";
      public boolean hasIp() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      public String getIp() {
        Object ref = ip_;
        if (!(ref instanceof String)) {
          String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
          ip_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      public Builder setIp(String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        ip_ = value;
        onChanged();
        return this;
      }
      public Builder clearIp() {
        bitField0_ = (bitField0_ & ~0x00000002);
        ip_ = getDefaultInstance().getIp();
        onChanged();
        return this;
      }
      void setIp(com.google.protobuf.ByteString value) {
        bitField0_ |= 0x00000002;
        ip_ = value;
        onChanged();
      }
      
      // required int32 port = 3;
      private int port_ ;
      public boolean hasPort() {
        return ((bitField0_ & 0x00000004) == 0x00000004);
      }
      public int getPort() {
        return port_;
      }
      public Builder setPort(int value) {
        bitField0_ |= 0x00000004;
        port_ = value;
        onChanged();
        return this;
      }
      public Builder clearPort() {
        bitField0_ = (bitField0_ & ~0x00000004);
        port_ = 0;
        onChanged();
        return this;
      }
      
      // repeated string services = 4;
      private com.google.protobuf.LazyStringList services_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      private void ensureServicesIsMutable() {
        if (!((bitField0_ & 0x00000008) == 0x00000008)) {
          services_ = new com.google.protobuf.LazyStringArrayList(services_);
          bitField0_ |= 0x00000008;
         }
      }
      public java.util.List<String>
          getServicesList() {
        return java.util.Collections.unmodifiableList(services_);
      }
      public int getServicesCount() {
        return services_.size();
      }
      public String getServices(int index) {
        return services_.get(index);
      }
      public Builder setServices(
          int index, String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  ensureServicesIsMutable();
        services_.set(index, value);
        onChanged();
        return this;
      }
      public Builder addServices(String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  ensureServicesIsMutable();
        services_.add(value);
        onChanged();
        return this;
      }
      public Builder addAllServices(
          Iterable<String> values) {
        ensureServicesIsMutable();
        super.addAll(values, services_);
        onChanged();
        return this;
      }
      public Builder clearServices() {
        services_ = com.google.protobuf.LazyStringArrayList.EMPTY;
        bitField0_ = (bitField0_ & ~0x00000008);
        onChanged();
        return this;
      }
      void addServices(com.google.protobuf.ByteString value) {
        ensureServicesIsMutable();
        services_.add(value);
        onChanged();
      }
      
      // optional string registerToken = 5;
      private Object registerToken_ = "";
      public boolean hasRegisterToken() {
        return ((bitField0_ & 0x00000010) == 0x00000010);
      }
      public String getRegisterToken() {
        Object ref = registerToken_;
        if (!(ref instanceof String)) {
          String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
          registerToken_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      public Builder setRegisterToken(String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000010;
        registerToken_ = value;
        onChanged();
        return this;
      }
      public Builder clearRegisterToken() {
        bitField0_ = (bitField0_ & ~0x00000010);
        registerToken_ = getDefaultInstance().getRegisterToken();
        onChanged();
        return this;
      }
      void setRegisterToken(com.google.protobuf.ByteString value) {
        bitField0_ |= 0x00000010;
        registerToken_ = value;
        onChanged();
      }
      
      // @@protoc_insertion_point(builder_scope:NSServer)
    }
    
    static {
      defaultInstance = new NSServer(true);
      defaultInstance.initFields();
    }
    
    // @@protoc_insertion_point(class_scope:NSServer)
  }
  
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_NSServer_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_NSServer_fieldAccessorTable;
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    String[] descriptorData = {
      "\n\032meta/naming_services.proto\"Y\n\010NSServer" +
      "\022\n\n\002id\030\001 \002(\t\022\n\n\002ip\030\002 \002(\t\022\014\n\004port\030\003 \002(\005\022\020" +
      "\n\010services\030\004 \003(\t\022\025\n\rregisterToken\030\005 \001(\tB" +
      "$\n\"com.cdg.kahui.craft.rpc.name.model.gen"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_NSServer_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_NSServer_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_NSServer_descriptor,
              new String[] { "Id", "Ip", "Port", "Services", "RegisterToken", },
              NSServer.class,
              NSServer.Builder.class);
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }
  
  // @@protoc_insertion_point(outer_class_scope)
}
