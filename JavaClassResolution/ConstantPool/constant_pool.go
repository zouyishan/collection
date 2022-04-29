package ConstantPool

import (
	"JavaClassResolution/Reader"
)

const (
	CONSTANT_UTF8_INFO            = 1
	CONSTANT_INTEGER_INFO         = 3
	CONSTANT_FLOAT_INFO           = 4
	CONSTANT_LONG_INFO            = 5
	CONSTANT_DOUBLE_INFO          = 6
	CONSTANT_CLASS_INFO           = 7
	CONSTANT_STRING_INGFO         = 8
	CONSTANT_FIELD_REF_INFO       = 9
	CONSTANT_METHOD_REF_INFO      = 10
	CONSTANT_INTERFACE_METHOD_REF = 11
	CONSTANT_NAME_AND_TYPE_INFO   = 12
	CONSTANT_METHOD_HANDLE_INFO   = 15
	CONSTANT_METHOD_TYPE_TYPE     = 16
	CONSTANT_INVOKE_DYNAMIC_INFO  = 18
)

type ConstantInfo interface {
	String() string
	ReadInfo(reader *Reader.ClassReader)
}

func Parse(reader *Reader.ClassReader, constantPoolCount uint16) []ConstantInfo {
	constantInfoList := make([]ConstantInfo, 0)
	// 从1开始
	for i := 1; uint16(i) < constantPoolCount; i++ {
		tag := uint8(reader.ReadUInt8())
		/**
		 * 如果一个CONSTANT_Long_info或CONSTANT_Double_info结构的项在常量池中的索引为n
		 * 则常量池中下一个有效的项的索引为n + 2，此时常量池中索引为n + 1的项有效但必须被认为不可用。
		 */
		if tag == CONSTANT_LONG_INFO || tag == CONSTANT_DOUBLE_INFO {
			constantInfoList = append(constantInfoList, nil)
			i++
		}
		constantInfo := detail(tag)
		constantInfo.ReadInfo(reader)
		constantInfoList = append(constantInfoList, constantInfo)
	}
	return constantInfoList
}

func detail(tag uint8) ConstantInfo {
	switch tag {
	case CONSTANT_UTF8_INFO:
		return NewConstantUtf8Info()
	case CONSTANT_INTEGER_INFO:
		return NewConstantIntegerInfo()
	case CONSTANT_FLOAT_INFO:
		return NewConstantFloatInfo()
	case CONSTANT_LONG_INFO:
		return NewConstantLongInfo()
	case CONSTANT_DOUBLE_INFO:
		return NewConstantDoubleInfo()
	case CONSTANT_CLASS_INFO:
		return NewConstantClassInfo()
	case CONSTANT_STRING_INGFO:
		return NewConstantStringInfo()
	case CONSTANT_FIELD_REF_INFO:
		return NewConstantFieldRefInfo()
	case CONSTANT_METHOD_REF_INFO:
		return NewConstantMethodRefInfo()
	case CONSTANT_INTERFACE_METHOD_REF:
		return NewConstantInterfaceMethodRefInfo()
	case CONSTANT_NAME_AND_TYPE_INFO:
		return NewConstantNameAndTypeInfo()
	case CONSTANT_METHOD_HANDLE_INFO:
		return NewConstantMethodHandleInfo()
	case CONSTANT_METHOD_TYPE_TYPE:
		return NewConstantMethodTypeInfo()
	case CONSTANT_INVOKE_DYNAMIC_INFO:
		return NewConstantInvokeDynamicInfo()
	default:
		panic("Cannot Find ConstantInfo, tag: " + string(tag))
	}
}
