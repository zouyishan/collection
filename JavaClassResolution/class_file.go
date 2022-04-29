package JavaClassResolution

import (
	"JavaClassResolution/Attribute"
	"JavaClassResolution/ConstantPool"
	"JavaClassResolution/Reader"
	"fmt"
	"strconv"
	"strings"
)

type ClassFile struct {
	Magic             uint32
	MinorVersion      uint16
	MajorVersion      uint16
	ConstantPoolCount uint16
	ConstantPool      []ConstantPool.ConstantInfo
	AccessFlags       uint16
	ThisClass         uint16
	SuperClass        uint16
	InterfacesCount   uint16
	Interfaces        []uint16
	FieldsCount       uint16
	Fields            []FieldInfo
	MethodCount       uint16
	Methods           []MethodInfo
	/**
	attribute_name_index指向常量池中的一个CONSTANT_Utf8_info类型的常量，该常量包含着属性的名称。
	在解析属性时，SmallVM正是通过attribute_name_index指向的常量对应的属性名称来得知属性的类型。
	*/
	AttributesCount uint16
	Attributes      []Attribute.AttributeInfo
}

type FieldInfo struct {
	AccessFlags     uint16
	NameIndex       uint16
	DescriptorIndex uint16
	AttributesCount uint16
	Attributes      []Attribute.AttributeInfo
}

type MethodInfo struct {
	AccessFlags     uint16
	NameIndex       uint16
	DescriptorIndex uint16
	AttributesCount uint16
	Attributes      []Attribute.AttributeInfo
}

func NewClassFile() *ClassFile {
	return &ClassFile{}
}

func (this *ClassFile) Parse(reader *Reader.ClassReader) (*ClassFile, error) {
	this.ReadMagic(reader)
	this.ReadMinorVersion(reader)
	this.ReadMajorVersion(reader)
	this.ReadConstantPoolCount(reader)
	this.ReadConstantPool(reader, this.ConstantPoolCount)
	this.ReadAccessFlag(reader)
	this.ReadThisClass(reader)
	this.ReadSuperClass(reader)

	this.ReadInterfacesCount(reader)
	this.ReadInterfaces(reader, this.InterfacesCount)

	this.ReadFieldsCount(reader)
	this.ReadFileds(reader, this.FieldsCount)

	this.ReadMethodCount(reader)
	this.ReadMethods(reader, this.MethodCount)

	this.ReadAttributesCount(reader)
	this.ReadAttributes(reader, this.AttributesCount)
	return this, nil
}

func (this *ClassFile) ReadMagic(reader *Reader.ClassReader) {
	val := reader.ReadUInt32()
	if val != 0xCAFEBABE {
		panic("java magic error")
	}
	this.Magic = val
}

func (this *ClassFile) ReadMinorVersion(reader *Reader.ClassReader) {
	val := reader.ReadUInt16()
	this.MinorVersion = val
}

func (this *ClassFile) ReadMajorVersion(reader *Reader.ClassReader) {
	val := reader.ReadUInt16()
	this.MajorVersion = val
}

func (this *ClassFile) ReadConstantPoolCount(reader *Reader.ClassReader) {
	val := reader.ReadUInt16()
	this.ConstantPoolCount = val
}

func (this *ClassFile) ReadConstantPool(reader *Reader.ClassReader, count uint16) {
	this.ConstantPool = ConstantPool.Parse(reader, count)
}

func (this *ClassFile) ReadAccessFlag(reader *Reader.ClassReader) {
	this.AccessFlags = reader.ReadUInt16()
}

func (this *ClassFile) ReadThisClass(reader *Reader.ClassReader) {
	this.ThisClass = reader.ReadUInt16()
}

func (this *ClassFile) ReadSuperClass(reader *Reader.ClassReader) {
	this.SuperClass = reader.ReadUInt16()
}

func (this *ClassFile) ReadInterfacesCount(reader *Reader.ClassReader) {
	this.InterfacesCount = reader.ReadUInt16()
}

func (this *ClassFile) ReadInterfaces(reader *Reader.ClassReader, count uint16) {
	interfaceArray := make([]uint16, 0)
	for i := 0; uint16(i) < count; i++ {
		temp := reader.ReadUInt16()
		interfaceArray = append(interfaceArray, temp)
	}
	this.Interfaces = interfaceArray
}

func (this *ClassFile) ReadFieldsCount(reader *Reader.ClassReader) {
	this.FieldsCount = reader.ReadUInt16()
}

func (this *ClassFile) ReadFileds(reader *Reader.ClassReader, count uint16) {
	this.Fields = make([]FieldInfo, 0)
	for i := 0; uint16(i) < count; i++ {
		fieldInfo := FieldInfo{}
		fieldInfo.AccessFlags = reader.ReadUInt16()
		fieldInfo.NameIndex = reader.ReadUInt16()
		fieldInfo.DescriptorIndex = reader.ReadUInt16()
		fieldInfo.AttributesCount = reader.ReadUInt16()
		fieldInfo.Attributes = Attribute.Parse(reader, this.ConstantPool, fieldInfo.AttributesCount)
		this.Fields = append(this.Fields, fieldInfo)
	}
}

func (this *ClassFile) ReadMethodCount(reader *Reader.ClassReader) {
	this.MethodCount = reader.ReadUInt16()
}

func (this *ClassFile) ReadMethods(reader *Reader.ClassReader, count uint16) {
	methodInfoList := make([]MethodInfo, 0)
	for i := 0; uint16(i) < count; i++ {
		methodInfo := MethodInfo{}
		methodInfo.AccessFlags = reader.ReadUInt16()
		methodInfo.NameIndex = reader.ReadUInt16()
		methodInfo.DescriptorIndex = reader.ReadUInt16()
		methodInfo.AttributesCount = reader.ReadUInt16()
		methodInfo.Attributes = Attribute.Parse(reader, this.ConstantPool, methodInfo.AttributesCount)
		methodInfoList = append(methodInfoList, methodInfo)
	}
	this.Methods = methodInfoList
}

func (this *ClassFile) ReadAttributesCount(reader *Reader.ClassReader) {
	this.AttributesCount = reader.ReadUInt16()
}

func (this *ClassFile) ReadAttributes(reader *Reader.ClassReader, count uint16) {
	this.Attributes = Attribute.Parse(reader, this.ConstantPool, count)
}

/**
=============================== 解析
*/
func (this *ClassFile) GetUtf8Value(index uint16) string {
	if index <= 0 || index >= this.ConstantPoolCount {
		panic("GetUtf8Value: Index Out of Bounds")
	}
	val := this.ConstantPool[index-1].String()
	return val
}

func (this *ClassFile) ParseThisClass() string {
	index, _ := strconv.Atoi(this.GetUtf8Value(this.ThisClass))
	fmt.Println(index)
	return this.GetUtf8Value(uint16(index))
}

func (this *ClassFile) ParseSuperClass() string {
	index, _ := strconv.Atoi(this.GetUtf8Value(this.SuperClass))
	return this.GetUtf8Value(uint16(index))
}

func (this *ClassFile) ParseInterfaces() string {
	var index int
	interfaces := make([]string, 0)
	for index = 0; index < len(this.Interfaces); index++ {
		interIndex, _ := strconv.Atoi(this.GetUtf8Value(this.Interfaces[index]))
		interfaces = append(interfaces, this.GetUtf8Value(uint16(interIndex)))
	}
	return strings.Join(interfaces, ", ")
}
