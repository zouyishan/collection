package Attribute

import (
	"JavaClassResolution/ConstantPool"
	"JavaClassResolution/Reader"
)

type AttributeInfo interface {
	ReadInfo(reader *Reader.ClassReader)
}

func Parse(reader *Reader.ClassReader, constantPool []ConstantPool.ConstantInfo, count uint16) []AttributeInfo {
	attributeList := make([]AttributeInfo, 0)
	for i := 0; uint16(i) < count; i++ {
		nameIndex := reader.ReadUInt16()
		name := constantPool[nameIndex-1].String()
		attributeInfo := NewAttributeInfo(nameIndex, name, constantPool)
		attributeInfo.ReadInfo(reader)
		attributeList = append(attributeList, attributeInfo)
	}
	return attributeList
}

func NewAttributeInfo(nameIndex uint16, name string, pool []ConstantPool.ConstantInfo) AttributeInfo {
	switch name {
	case "Code":
		// code 也会有属性在属性表之中，so，参数要加上pool
		return NewCode(pool, nameIndex)
	case "Exceptions":
		return NewExceptions(nameIndex)
	case "LineNumberTable":
		return NewLineNumberTable(nameIndex)
	case "LocalVariableTable":
		return NewLocalVariableTable(nameIndex)
	case "LocalVariableTypeTable":
		return NewLocalVariableTypeTable(nameIndex)
	case "SourceFile":
		return NewSourceFile(nameIndex)
	case "ConstantValue":
		return NewConstantValue(nameIndex)
	case "InnerClasses":
		return NewInnerClasses(nameIndex)
	case "Deprecated":
		return NewDeprecated(nameIndex)
	case "Synthetic":
		return NewSynthetic(nameIndex)
	case "Signature":
		return NewSignature(nameIndex)
	case "BootstrapMethods":
		return NewBootstrapMethods(nameIndex)
	default: // case "StackMapTable"
		// fmt.Printf("Get Unparsed Attribute: name = %s\n", name)
		return NewUnparsed(nameIndex)
	}
}
