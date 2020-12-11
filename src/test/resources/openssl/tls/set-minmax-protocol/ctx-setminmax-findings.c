#include <openssl/ssl.h>

# define TLS1_VERSION                    0x0301
# define TLS1_1_VERSION                  0x0302
# define TLS1_2_VERSION                  0x0303
# define TLS1_3_VERSION                  0x0304
# define TLS_MAX_VERSION                 TLS1_3_VERSION

int ctx_set_V1_1_3(SSL_CTX *ctx) {
 return SSL_CTX_set_min_proto_version(ctx, TLS1_1_VERSION) && SSL_CTX_set_max_proto_version(ctx, TLS1_3_VERSION);
}

int ctx_set_V1__1(SSL_CTX *ctx) {
 return SSL_CTX_set_min_proto_version(ctx, TLS1_VERSION) && SSL_CTX_set_max_proto_version(ctx, TLS1_1_VERSION);
}


int ssl_set_V1_1_3(SSL *ssl){
    return SSL_set_min_proto_version(ssl, TLS1_1_VERSION) && SSL_set_max_proto_version(ssl, TLS1_3_VERSION);
}

int ssl_set_V1__1(SSL *ssl){
    return SSL_set_min_proto_version(ssl, TLS1_VERSION) && SSL_set_max_proto_version(ssl, TLS1_1_VERSION);
}



int ctx_set_V3_1_3(SSL_CTX *ctx) {
 return SSL_CTX_set_min_proto_version(ctx, SSL3_VERSION) && SSL_CTX_set_max_proto_version(ctx, TLS1_3_VERSION);
}

int ctx_set_V3_3(SSL_CTX *ctx) {
 return SSL_CTX_set_min_proto_version(ctx, SSL3_VERSION) && SSL_CTX_set_max_proto_version(ctx, SSL3_VERSION);
}


int ssl_set_V3_1_3(SSL *ssl){
    return SSL_set_min_proto_version(ssl, SSL3_VERSION) && SSL_set_max_proto_version(ssl, TLS1_3_VERSION);
}

int ssl_set_V3_3(SSL *ssl){
    return SSL_set_min_proto_version(ssl, SSL3_VERSION) && SSL_set_max_proto_version(ssl, SSL3_VERSION);
}

