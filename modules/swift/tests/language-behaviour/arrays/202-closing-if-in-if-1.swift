(int r) f(int[] b) {
	foreach i in b {
		trace(i);
	}
	r = 1;
}


int[] a;
int r;

if (true) {
	if (false) {
		a[0] = 1;
	}
	r = f(a);
}

