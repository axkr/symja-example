mkdir build
cd build || exit 2

rm -rf jlatex
git clone https://github.com/tranleduy2000/jlatex.git jlatex || exit 2

cp ../../../local.properties ./jlatex/local.properties
cd jlatex || exit 2

chmod +x gradlew
./gradlew assembleDebug

cp ./jlatexmath/build/outputs/aar/jlatexmath-debug.aar ../../jlatexmath-debug.aar
cp ./jlatexmath-font-cyrillic/build/outputs/aar/jlatexmath-font-cyrillic-debug.aar ../../jlatexmath-font-cyrillic-debug.aar
cp ./jlatexmath-font-greek/build/outputs/aar/jlatexmath-font-greek-debug.aar ../../jlatexmath-font-greek-debug.aar
