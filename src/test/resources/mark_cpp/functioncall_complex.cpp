int call(int fd) {
	return 2;
}

int bar(int i) {
	return 2;
}


int main() {
	call(17);
	bar(12);
}


int baz() {
    call(11);
    bar(10);
}