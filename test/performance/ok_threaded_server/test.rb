@config = {
  :app => "bim.apps.EverythingsOKApp",
  :description => "Multi-threaded, always returns 200",
  :args => [ "8080", "127.0.0.1", "threaded" ],
  :urls => {
    "http://127.0.0.1:8080" => 200,
    "http://127.0.0.1:8080/foo/bar" => 200,
  },
}
