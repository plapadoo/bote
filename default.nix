let
  pkgs = import <nixpkgs> {};
  # src = pkgs.fetchFromGitHub {
  #   owner = "plapadoo";
  #   repo = "bote";
  #   rev = "HEAD";
  #   sha256 = "1qb8cchfdk2ln4pzil2dl5m2jmm4bqad6pfb8a5bc122x58y0bgf";
  # };
  src = ./.;
  # Force build to use repo.maven.apache.org, because some
  # "plexus-interpolation" plugins wants to be loaded from
  # jvnet-nexus, but gives a 404, which halts the maven download
  # (wtf?)
  settings = pkgs.writeText "settings.xml" ''
    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                          http://maven.apache.org/xsd/settings-1.0.0.xsd">
      <mirrors>
        <mirror>
          <id>internal-repository</id>
          <name>Maven Repository Manager running on repo.mycompany.com</name>
          <url>https://repo.maven.apache.org/maven2</url>
          <mirrorOf>*</mirrorOf>
        </mirror>
      </mirrors>
    </settings>
  '';
  deps = pkgs.stdenv.mkDerivation {
    name = "bote-deps";
    inherit src;
    buildInputs = [ pkgs.jdk pkgs.maven ];
    buildPhase = ''
        mvn package -Dmaven.repo.local=$out/.m2 --settings ${settings}
    '';
    # keep only *.{pom,jar,sha1,nbm} and delete all ephemeral files with lastModified timestamps inside
    installPhase = ''find $out/.m2 -type f -regex '.+\(\.lastUpdated\|resolver-status\.properties\|_remote\.repositories\)' -delete'';
    outputHashAlgo = "sha256";
    outputHashMode = "recursive";
    outputHash = "018jw6ggjh1n96nl5gmsvajbdy327mkl5nq16a05c2jai4z07wxi";
  };
in pkgs.stdenv.mkDerivation rec {
   version = "0.1";
   name = "bote-${version}";
   
   inherit src;

   buildInputs = [ pkgs.jdk pkgs.maven pkgs.makeWrapper pkgs.jre ];

   buildPhase = ''
     # 'maven.repo.local' must be writable so copy it out of nix store
     mvn package --offline -Dmaven.repo.local=$(cp -dpR ${deps}/.m2 ./ && chmod +w -R .m2 && pwd)/.m2
   '';

   installPhase = ''
     mkdir -p $out/bin
     mkdir -p $out/share
     cp target/bote-${version}-jar-with-dependencies.jar $out/share/
     makeWrapper ${pkgs.jre}/bin/java $out/bin/bote --add-flags "-jar $out/share/bote-${version}-jar-with-dependencies.jar"
   '';
         #--add-flags "-cp $out/share/bote-${version}-jar-with-dependencies.jar de.plapadoo.bote.ApplicationKt"

  meta = with pkgs.stdenv.lib; {
    description = "subscription backend";
    homepage = https://github.com/plapadoo/bote;
    license = licenses.apache;
    #maintainers = [ maintainers.pmiddend ];
    platforms = [ "x86_64-linux" ];
  };
}
