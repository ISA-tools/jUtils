# Make the javadocs of each module under the main docs directory.
# This is usually run by Travis during builds

echo -e "\n\n\t---- Making Javadocs for jutils\n"

set -e 
set -o pipefail

export JAVADOC_REL_PATH=docs/javadocs
cd "$(dirname $0)"

for mod in jutils jutils-io jutils-j2ee
do
	echo -e "\n\n\tProcessing '$mod'\n"
	rm -Rf "$JAVADOC_REL_PATH/$mod"
	cd "$mod"
	mvn javadoc:javadoc -DreportOutputDirectory="../$JAVADOC_REL_PATH"
	cd ..
done

echo -e "\n\n\tThe End\n"
