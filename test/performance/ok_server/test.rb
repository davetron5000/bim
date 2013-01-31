@config = {
  :app => "bim.apps.EverythingsOKApp",
  :args => [ "8080", "127.0.0.1" ],
  :urls => {
    "http://127.0.0.1:8080" => 200,
    "http://127.0.0.1:8080/foo/bar" => 200,
  },
}
