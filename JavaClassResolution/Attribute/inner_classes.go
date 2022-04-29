package Attribute

import (
	"JavaClassResolution/Reader"
)

type InnerClasses struct {
	attributeNameIndex uint16
	attributeLength    uint32
	numberOfClasses    uint16
	innerClasses       []InnerClassesInfo
}

type InnerClassesInfo struct {
	innerClassInfoIndex   uint16
	outerClassInfoIndex   uint16
	innerNameIndex        uint16
	innerClassAccessFlags uint16
}

func NewInnerClasses(nameIndex uint16) *InnerClasses {
	return &InnerClasses{attributeNameIndex: nameIndex}
}

func (this *InnerClasses) ReadInfo(reader *Reader.ClassReader) {
	this.attributeLength = reader.ReadUInt32()
	this.numberOfClasses = reader.ReadUInt16()
	this.innerClasses = this.readInnerClasses(reader, this.numberOfClasses)
}

func (this *InnerClasses) readInnerClasses(reader *Reader.ClassReader, num uint16) []InnerClassesInfo {
	var index uint16
	classes := make([]InnerClassesInfo, 0)
	for index = 0; index < num; index++ {
		class := InnerClassesInfo{}
		class.innerClassInfoIndex = reader.ReadUInt16()
		class.outerClassInfoIndex = reader.ReadUInt16()
		class.innerNameIndex = reader.ReadUInt16()
		class.innerClassAccessFlags = reader.ReadUInt16()
		classes = append(classes, class)
	}
	return classes
}
