package Attribute

import (
	"JavaClassResolution/Reader"
)

type Unparsed struct {
	attributeNameIndex uint16
	attributeLength    uint32
	info               []byte
}

func NewUnparsed(nameIndex uint16) *Unparsed {
	return &Unparsed{attributeNameIndex: nameIndex}
}

func (this *Unparsed) ReadInfo(reader *Reader.ClassReader) {
	this.attributeLength = reader.ReadUInt32()
	this.info = reader.ReadBytes(this.attributeLength)
}
