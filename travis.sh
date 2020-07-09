set -e
mvn deploy --settings settings.xml

if [[ "$TRAVIS_PULL_REQUEST" != "false" ]] || [[ "$TRAVIS_BRANCH" != 'master' ]]; then
	echo -e "\nThis isn't main-repo/master, skipping Javadoc\n"
	exit
fi 
	
./mk-javadocs.sh
git remote set-url origin https://marco-brandizi:$REPO_PWD@github.com/ISA-tools/jUtils 
git commit -a -m 'Updating auto-generated files from Travis [ci skip]'
git push origin HEAD:"$TRAVIS_BRANCH"
