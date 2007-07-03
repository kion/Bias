cp="";
file=conf/classpath.conf;
if [ -e $file ]
then
  cp=`cat $file`;
  if [ "${#cp}" != "0" ]
  then
    cp=:$cp;
  fi
fi
cmdLine="java -cp bias.jar$cp bias.Launcher";
echo $cmdLine\n;
`$cmdLine`;
