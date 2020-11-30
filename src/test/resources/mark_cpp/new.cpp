class MyObject{
public:
    void DoSomething(int);
};

int main() {
    MyObject* ctx;

    ctx = new MyObject();
    ctx->DoSomething(0);
}