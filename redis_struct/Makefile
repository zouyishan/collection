# 鸡肋CMakeLists.txt，不添加没法做检查，两个main不可用
sdsTest: test/sdsTest.c redis_struct/sds.c redis_struct/sds.h redis_struct/zmalloc.c
	gcc test/sdsTest.c -o sdsTest;\
	./sdsTest;\
	rm -rf sdsTest

# 分配内存
C_FLAGS := -DzysTest=\"fdsaf\"

sdsMallocTest: redis_struct/sds.c redis_struct/zmalloc.c
	gcc $(C_FLAGS) redis_struct/sds.c redis_struct/zmalloc.c -o sdsMallocTest;\
	./sdsMallocTest;\
	rm -rf sdsMallocTest

# 原子类多线程测试
sdsThreadTest: test/threadTest.c
	gcc test/threadTest.c -o threadTest;\
	./threadTest;\
	rm -rf threadTest