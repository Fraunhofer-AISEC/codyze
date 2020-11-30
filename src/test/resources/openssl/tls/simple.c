int main() {
    SSL_CTX *ctx;

    method = SSLv23_server_method();

    ctx = SSL_CTX_new(method);

    SSL_CTX_set_ecdh_auto(ctx, 1);
}