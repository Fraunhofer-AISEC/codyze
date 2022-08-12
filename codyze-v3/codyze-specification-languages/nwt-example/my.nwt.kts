nwt {
  account("checking", "Checking")
  account("savings", "Savings")

  snapshots {
    on("2022-01-01") {
      balance("checking", 1000.0)
      balance("savings", 2000.0)
    }
    on("2022-02-01") {
      balance("checking", 1200.0)
      balance("savings", 2500.0)
    }
  }

  report {
    accounts()
    snapshots()
  }
}
