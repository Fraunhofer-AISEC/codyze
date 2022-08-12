package main

import (
	"context"
	"log"
	"net/http"

	jwtmiddleware "github.com/auth0/go-jwt-middleware/v2"
	"github.com/auth0/go-jwt-middleware/v2/validator"
	"gorm.io/driver/sqlite"
	"gorm.io/gorm"
)

var db *gorm.DB

type ImportantInfo struct {
	gorm.Model

	VeryImportant bool
	Information   string
}

func main() {
	var err error

	db, err = gorm.Open(sqlite.Open("file::memory:?cache=shared"), &gorm.Config{})
	if err != nil {
		log.Fatalf("Error while opening database: %v", err)
	}

	db.AutoMigrate(&ImportantInfo{})

	jwtValidator, err := validator.New(
		func(ctx context.Context) (interface{}, error) {
			// Our token must be signed using this data.
			return []byte("secret"), nil
		},
		validator.HS256,
		"myissuer",
		[]string{"myaudience"},
	)
	if err != nil {
		log.Fatalf("Could not create validator: %v", err)
	}

	middleware := jwtmiddleware.New(jwtValidator.ValidateToken)

	http.HandleFunc("/do", do)

	http.ListenAndServe(":8080", middleware.CheckJWT(http.DefaultServeMux))
}

func do(w http.ResponseWriter, r *http.Request) {
	var claims *validator.ValidatedClaims
	var user string

	db.Create(&ImportantInfo{
		VeryImportant: true,
		Information:   "Some Information",
	})

	claims = r.Context().Value(jwtmiddleware.ContextKey{}).(*validator.ValidatedClaims)
	user = claims.RegisteredClaims.Subject

	log.Printf("[audit] %s has created an important information", user)
}
