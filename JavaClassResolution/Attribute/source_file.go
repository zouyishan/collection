package Attribute

import (
	"JavaClassResolution/Reader"
)

type SourceFile struct {
	attributeNameIndex uint16
	attributeLength    uint32
	sourceFileIndex    uint16
}

func NewSourceFile(nameIndex uint16) *SourceFile {
	return &SourceFile{attributeNameIndex: nameIndex}
}

func (this *SourceFile) ReadInfo(reader *Reader.ClassReader) {
	this.attributeLength = reader.ReadUInt32()
	this.sourceFileIndex = reader.ReadUInt16()
}
