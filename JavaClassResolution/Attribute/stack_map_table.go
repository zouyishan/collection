package Attribute

import "JavaClassResolution/Reader"

type StackMapTable struct {
	attributeNameIndex   uint16
	attributeLength      uint32
	numberOfEntries      uint16
	stackMapFrameEntries []StackMapFrame
}

type StackMapFrame struct {
}

func NewStackMapTable(nameIndex uint16) *StackMapTable {
	return &StackMapTable{attributeNameIndex: nameIndex}
}

func (self *StackMapTable) readInfo(reader *Reader.ClassReader) {
	panic("Fail to read StackMapTable info!")
}
