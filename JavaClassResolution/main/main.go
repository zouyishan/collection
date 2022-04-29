package main

import (
	"JavaClassResolution"
	"JavaClassResolution/AccessFlag"
	"JavaClassResolution/Reader"
	"fmt"
	"os"
)

func ReadFile(fileName string) ([]byte, error) {
	file, err := os.Open("HelloWorld.class")
	if err != nil {
		return nil, err
	}
	defer file.Close()

	stats, err := file.Stat()
	if err != nil {
		return nil, err
	}

	data := make([]byte, stats.Size())
	count, err := file.Read(data)
	if err != nil {
		return nil, err
	}
	fmt.Printf("read file %s, len: %d \n", fileName, count)
	return data, nil
}

func viewClass(c *JavaClassResolution.ClassFile) {
	fmt.Printf("magic: %x\n", c.Magic)
	fmt.Printf("minor version: %d\n", c.MinorVersion)
	fmt.Printf("major version: %d\n", c.MajorVersion)
	fmt.Printf("constant pool count: %d\n", c.ConstantPoolCount)
	fmt.Printf("access flags: %s\n", AccessFlag.GetClassAccessFlags(c.AccessFlags))
	fmt.Printf("this class: %s\n", c.ParseThisClass())
	fmt.Printf("super class: %s\n", c.ParseSuperClass())
	fmt.Printf("interface count: %d\n", c.InterfacesCount)
	fmt.Printf("interfaces: %s\n", c.ParseInterfaces())
	fmt.Printf("fields count: %d\n", c.FieldsCount)
	viewField(c)
	fmt.Printf("method count: %d\n", c.MethodCount)
	viewMethod(c)
}

func viewField(c *JavaClassResolution.ClassFile) {
	for _, field := range c.Fields {
		fmt.Println()
		fmt.Printf("field name: %s\n", c.GetUtf8Value(field.NameIndex))
		fmt.Printf("access flag: %s\n", AccessFlag.GetFieldAccessFlags(field.AccessFlags))
		fmt.Printf("description: %s\n", c.GetUtf8Value(field.DescriptorIndex))
	}
	fmt.Println()
}

func viewMethod(c *JavaClassResolution.ClassFile) {
	for _, method := range c.Methods {
		fmt.Println()
		fmt.Printf("method name: %s\n", c.GetUtf8Value(method.NameIndex))
		fmt.Printf("access flags: %s\n", AccessFlag.GetMethodAccessFlags(method.AccessFlags))
		fmt.Printf("description: %s\n", c.GetUtf8Value(method.DescriptorIndex))
	}
	fmt.Println()
}

func viewAttribute(c *JavaClassResolution.ClassFile) {

}

func main() {
	data, err := ReadFile("HelloWorld.class")
	if err != nil {
		fmt.Println("ReadFile error")
	}
	r := Reader.NewClassReader(data)

	c := JavaClassResolution.NewClassFile()
	c, e := c.Parse(r)
	if e != nil {
		fmt.Println("Parse error")
	}
	if c == nil {
		panic("c == nil")
	}
	viewClass(c)

	fmt.Printf("AttributeCount: %d\n", c.AttributesCount)
}
