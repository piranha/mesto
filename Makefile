pub:
	rm -rf target/*.jar
	lein jar pom
	scp target/mesto-*.jar pom.xml clojars@clojars.org:
