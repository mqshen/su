/**
 * Autogenerated by Thrift
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package com.twitter.finagle.thrift.thrift;

import org.apache.thrift.*;
import org.apache.thrift.meta_data.FieldMetaData;
import org.apache.thrift.meta_data.ListMetaData;
import org.apache.thrift.meta_data.StructMetaData;
import org.apache.thrift.protocol.*;

import java.util.*;

// No additional import required for struct/union.

/**
 * The Response carries a reply header for tracing. These are
 * empty unless the request is being debugged, in which case a
 * transcript is copied.
 */
public class ResponseHeader implements TBase<ResponseHeader, ResponseHeader._Fields>, java.io.Serializable, Cloneable {
  private static final TStruct STRUCT_DESC = new TStruct("ResponseHeader");

  private static final TField SPANS_FIELD_DESC = new TField("spans", TType.LIST, (short)1);
  private static final TField CONTEXTS_FIELD_DESC = new TField("contexts", TType.LIST, (short)2);

  public List<Span> spans;
  public List<RequestContext> contexts;

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements TFieldIdEnum {
    SPANS((short)1, "spans"),
    CONTEXTS((short)2, "contexts");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // SPANS
          return SPANS;
        case 2: // CONTEXTS
          return CONTEXTS;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments

  public static final Map<_Fields, FieldMetaData> metaDataMap;
  static {
    Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.SPANS, new FieldMetaData("spans", TFieldRequirementType.DEFAULT,
        new ListMetaData(TType.LIST,
            new StructMetaData(TType.STRUCT, Span.class))));
    tmpMap.put(_Fields.CONTEXTS, new FieldMetaData("contexts", TFieldRequirementType.DEFAULT,
        new ListMetaData(TType.LIST,
            new StructMetaData(TType.STRUCT, RequestContext.class))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    FieldMetaData.addStructMetaDataMap(ResponseHeader.class, metaDataMap);
  }

  public ResponseHeader() {
  }

  public ResponseHeader(
    List<Span> spans,
    List<RequestContext> contexts)
  {
    this();
    this.spans = spans;
    this.contexts = contexts;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public ResponseHeader(ResponseHeader other) {
    if (other.isSetSpans()) {
      List<Span> __this__spans = new ArrayList<Span>();
      for (Span other_element : other.spans) {
        __this__spans.add(new Span(other_element));
      }
      this.spans = __this__spans;
    }
    if (other.isSetContexts()) {
      List<RequestContext> __this__contexts = new ArrayList<RequestContext>();
      for (RequestContext other_element : other.contexts) {
        __this__contexts.add(new RequestContext(other_element));
      }
      this.contexts = __this__contexts;
    }
  }

  public ResponseHeader deepCopy() {
    return new ResponseHeader(this);
  }

  @Override
  public void clear() {
    this.spans = null;
    this.contexts = null;
  }

  public int getSpansSize() {
    return (this.spans == null) ? 0 : this.spans.size();
  }

  public java.util.Iterator<Span> getSpansIterator() {
    return (this.spans == null) ? null : this.spans.iterator();
  }

  public void addToSpans(Span elem) {
    if (this.spans == null) {
      this.spans = new ArrayList<Span>();
    }
    this.spans.add(elem);
  }

  public List<Span> getSpans() {
    return this.spans;
  }

  public ResponseHeader setSpans(List<Span> spans) {
    this.spans = spans;
    return this;
  }

  public void unsetSpans() {
    this.spans = null;
  }

  /** Returns true if field spans is set (has been asigned a value) and false otherwise */
  public boolean isSetSpans() {
    return this.spans != null;
  }

  public void setSpansIsSet(boolean value) {
    if (!value) {
      this.spans = null;
    }
  }

  public int getContextsSize() {
    return (this.contexts == null) ? 0 : this.contexts.size();
  }

  public java.util.Iterator<RequestContext> getContextsIterator() {
    return (this.contexts == null) ? null : this.contexts.iterator();
  }

  public void addToContexts(RequestContext elem) {
    if (this.contexts == null) {
      this.contexts = new ArrayList<RequestContext>();
    }
    this.contexts.add(elem);
  }

  public List<RequestContext> getContexts() {
    return this.contexts;
  }

  public ResponseHeader setContexts(List<RequestContext> contexts) {
    this.contexts = contexts;
    return this;
  }

  public void unsetContexts() {
    this.contexts = null;
  }

  /** Returns true if field contexts is set (has been asigned a value) and false otherwise */
  public boolean isSetContexts() {
    return this.contexts != null;
  }

  public void setContextsIsSet(boolean value) {
    if (!value) {
      this.contexts = null;
    }
  }

  @SuppressWarnings("unchecked")
  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case SPANS:
      if (value == null) {
        unsetSpans();
      } else {
        setSpans((List<Span>)value);
      }
      break;

    case CONTEXTS:
      if (value == null) {
        unsetContexts();
      } else {
        setContexts((List<RequestContext>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case SPANS:
      return getSpans();

    case CONTEXTS:
      return getContexts();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been asigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case SPANS:
      return isSetSpans();
    case CONTEXTS:
      return isSetContexts();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof ResponseHeader)
      return this.equals((ResponseHeader)that);
    return false;
  }

  public boolean equals(ResponseHeader that) {
    if (that == null)
      return false;

    boolean this_present_spans = true && this.isSetSpans();
    boolean that_present_spans = true && that.isSetSpans();
    if (this_present_spans || that_present_spans) {
      if (!(this_present_spans && that_present_spans))
        return false;
      if (!this.spans.equals(that.spans))
        return false;
    }

    boolean this_present_contexts = true && this.isSetContexts();
    boolean that_present_contexts = true && that.isSetContexts();
    if (this_present_contexts || that_present_contexts) {
      if (!(this_present_contexts && that_present_contexts))
        return false;
      if (!this.contexts.equals(that.contexts))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(ResponseHeader other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    ResponseHeader typedOther = (ResponseHeader)other;

    lastComparison = Boolean.valueOf(isSetSpans()).compareTo(typedOther.isSetSpans());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSpans()) {
      lastComparison = TBaseHelper.compareTo(this.spans, typedOther.spans);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetContexts()).compareTo(typedOther.isSetContexts());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetContexts()) {
      lastComparison = TBaseHelper.compareTo(this.contexts, typedOther.contexts);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(TProtocol iprot) throws TException {
    TField field;
    iprot.readStructBegin();
    while (true)
    {
      field = iprot.readFieldBegin();
      if (field.type == TType.STOP) {
        break;
      }
      switch (field.id) {
        case 1: // SPANS
          if (field.type == TType.LIST) {
            {
              TList _list16 = iprot.readListBegin();
              this.spans = new ArrayList<Span>(_list16.size);
              for (int _i17 = 0; _i17 < _list16.size; ++_i17)
              {
                Span _elem18;
                _elem18 = new Span();
                _elem18.read(iprot);
                this.spans.add(_elem18);
              }
              iprot.readListEnd();
            }
          } else {
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: // CONTEXTS
          if (field.type == TType.LIST) {
            {
              TList _list19 = iprot.readListBegin();
              this.contexts = new ArrayList<RequestContext>(_list19.size);
              for (int _i20 = 0; _i20 < _list19.size; ++_i20)
              {
                RequestContext _elem21;
                _elem21 = new RequestContext();
                _elem21.read(iprot);
                this.contexts.add(_elem21);
              }
              iprot.readListEnd();
            }
          } else {
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        default:
          TProtocolUtil.skip(iprot, field.type);
      }
      iprot.readFieldEnd();
    }
    iprot.readStructEnd();

    // check for required fields of primitive type, which can't be checked in the validate method
    validate();
  }

  public void write(TProtocol oprot) throws TException {
    validate();

    oprot.writeStructBegin(STRUCT_DESC);
    if (this.spans != null) {
      oprot.writeFieldBegin(SPANS_FIELD_DESC);
      {
        oprot.writeListBegin(new TList(TType.STRUCT, this.spans.size()));
        for (Span _iter22 : this.spans)
        {
          _iter22.write(oprot);
        }
        oprot.writeListEnd();
      }
      oprot.writeFieldEnd();
    }
    if (this.contexts != null) {
      oprot.writeFieldBegin(CONTEXTS_FIELD_DESC);
      {
        oprot.writeListBegin(new TList(TType.STRUCT, this.contexts.size()));
        for (RequestContext _iter23 : this.contexts)
        {
          _iter23.write(oprot);
        }
        oprot.writeListEnd();
      }
      oprot.writeFieldEnd();
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ResponseHeader(");
    boolean first = true;

    sb.append("spans:");
    if (this.spans == null) {
      sb.append("null");
    } else {
      sb.append(this.spans);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("contexts:");
    if (this.contexts == null) {
      sb.append("null");
    } else {
      sb.append(this.contexts);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws TException {
    // check for required fields
  }

}

