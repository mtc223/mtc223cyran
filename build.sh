rm mtc223/*.class
echo "Starting compilation"
cd Capacity
javac Capacity.java
echo "Compilation successful, moving target files"
cd ..
mv Capacity/Capacity.class mtc223
echo "Class files moved, creating jar"
cd mtc223
jar cfmv Capacity.jar Manifest.txt Capacity.class
echo "jar created"
read -p "Enter to finish"