# NPGraph

为每一个method添加entry和exit节点，hashcode分别为正负method.hashcode
对entry节点处理：main：0->allPoint，
				others：0->0, args->args, this->this, 0->others
对exit节点处理：	所有return都指向exit，exit增加一个.return的节点，每个return的值连接到.return，return null时0 -> .return
array和field未进行相应处理：	不区分具体的array[index]，只区分array与array[index]:增加一个代表所有array[index]的节点
							对于field，应为每个属性增加节点：t.a，t.b。但不好获取t的属性，以及节点数太多
							t1.a = b; t2 = t1; c = t2.a;  应该有b-->c的边
过程间：	st: t1 = t2.func(t3, t4);
		增加st -> -st(没有t1 -> t1); -st -> succst；
		t2 -> func.this; t3 -> func.par0; t4 -> func.par1;
		.return(func.exit) -> t1(-st);
		对于var = func(); 若var后续没有use，该st不是Assign，而是invoke，不用处理