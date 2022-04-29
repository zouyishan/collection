package Attribute

import (
	"JavaClassResolution/Reader"
)

type Exceptions struct {
	attributeNameIndex  uint16
	attributeLength     uint32
	numberOfExceptions  uint16
	exceptionIndexTable []uint16
}

func NewExceptions(nameIndex uint16) *Exceptions {
	return &Exceptions{attributeNameIndex: nameIndex}
}

func (this *Exceptions) ReadInfo(reader *Reader.ClassReader) {
	this.attributeLength = reader.ReadUInt32()
	this.numberOfExceptions = reader.ReadUInt16()
	this.exceptionIndexTable = make([]uint16, 0)
	for i := 0; uint16(i) < this.numberOfExceptions; i++ {
		this.exceptionIndexTable = append(this.exceptionIndexTable, reader.ReadUInt16())
	}
}
