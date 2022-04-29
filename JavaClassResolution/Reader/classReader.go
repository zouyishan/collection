package Reader

import "encoding/binary"

type ClassReader struct {
	data []byte
}

func NewClassReader(data []byte) *ClassReader {
	return &ClassReader{data}
}

func (this *ClassReader) ReadUInt8() uint8 {
	val := this.data[0]
	this.data = this.data[1:]
	return val
}

func (this *ClassReader) ReadUInt16() uint16 {
	val := binary.BigEndian.Uint16(this.data)
	this.data = this.data[2:]
	return val
}

func (this *ClassReader) ReadUInt32() uint32 {
	val := binary.BigEndian.Uint32(this.data)
	this.data = this.data[4:]
	return val
}

func (this *ClassReader) ReadUInt64() uint64 {
	val := binary.BigEndian.Uint64(this.data)
	this.data = this.data[8:]
	return val
}

func (this *ClassReader) ReadBytes(size uint32) []byte {
	val := this.data[:size]
	this.data = this.data[size:]
	return val
}

func (this *ClassReader) Length() int {
	return len(this.data)
}
