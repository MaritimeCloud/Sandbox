#!/bin/sh
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


# OPTIONS:
#   -f: start in foreground
#   -p <filename>: log the pid to a file (useful to kill it later)
#   -v: print version string and exit

# CONTROLLING STARTUP:
# 
# This script relies on few environment variables to determine startup
# behavior, those variables are:
#
#   CLASSPATH -- A Java classpath containing everything necessary to run.
#   JVM_OPTS -- Additional arguments to the JVM for heap size, etc
#   MC_CLIENT_CONF -- Directory containing Cassandra configuration files.
#
# As a convenience, a fragment of shell is sourced in order to set one or
# more of these variables. This so-called `include' can be placed in a 
# number of locations and will be searched for in order. The lowest 
# priority search path is the same directory as the startup script, and
# since this is the location of the sample in the project tree, it should
# almost work Out Of The Box.
#
# Any serious use-case though will likely require customization of the
# include. For production installations, it is recommended that you copy
# the sample to one of /usr/share/mc-client/mc-client.in.sh,
# /usr/local/share/mc-client/mc-client.in.sh, or 
# /opt/mc-client/mc-client.in.sh and make your modifications there.
#
# Another option is to specify the full path to the include file in the
# environment. For example:
#
#   $ MC_CLIENT_INCLUDE=/path/to/in.sh mc-client -p /var/run/cass.pid
#
# Note: This is particularly handy for running multiple instances on a 
# single installation, or for quick tests.
#
# Finally, developers and enthusiasts who frequently run from an SVN 
# checkout, and do not want to locally modify bin/mc-client.in.sh, can put
# a customized include file at ~/.mc-client.in.sh.
#
# If you would rather configure startup entirely from the environment, you
# can disable the include by exporting an empty MC_CLIENT_INCLUDE, or by 
# ensuring that no include files exist in the aforementioned search list.
# Be aware that you will be entirely responsible for populating the needed
# environment variables.

# NB: Developers should be aware that this script should remain compatible with
# POSIX sh and Solaris sh. This means, in particular, no $(( )) and no $( ).

# If an include wasn't specified in the environment, then search for one...
if [ "x$MC_CLIENT_INCLUDE" = "x" ]; then
    # Locations (in order) to use when searching for an include file.
    for include in "`dirname "$0"`/mc-client.in.sh" \
                   "$HOME/.mc-client.in.sh" \
                   /usr/share/mc-client/mc-client.in.sh \
                   /usr/local/share/mc-client/mc-client.in.sh \
                   /opt/mc-client/mc-client.in.sh; do
        if [ -r "$include" ]; then
            . "$include"
            break
        fi
    done
# ...otherwise, source the specified include.
elif [ -r "$MC_CLIENT_INCLUDE" ]; then
    . "$MC_CLIENT_INCLUDE"
fi

# Use JAVA_HOME if set, otherwise look for java in PATH
if [ -n "$JAVA_HOME" ]; then
    # Why we can't have nice things: Solaris combines x86 and x86_64
    # installations in the same tree, using an unconventional path for the
    # 64bit JVM.  Since we prefer 64bit, search the alternate path first,
    # (see https://issues.apache.org/jira/browse/MC_CLIENT-4638).
    for java in "$JAVA_HOME"/bin/amd64/java "$JAVA_HOME"/bin/java; do
        if [ -x "$java" ]; then
            JAVA="$java"
            break
        fi
    done
else
    JAVA=java
fi

if [ -z $JAVA ] ; then
    echo Unable to find java executable. Check JAVA_HOME and PATH environment variables. > /dev/stderr
    exit 1;
fi

if [ -z "$MC_CLIENT_CONF" ]; then
    echo "You must set the MC_CLIENT_CONF and CLASSPATH vars" >&2
    exit 1
fi

if [ -f "$MC_CLIENT_CONF/mc-client-env.sh" ]; then
    . "$MC_CLIENT_CONF/mc-client-env.sh"
fi

# Special-case path variables.
case "`uname`" in
    CYGWIN*) 
        CLASSPATH=`cygpath -p -w "$CLASSPATH"`
        MC_CLIENT_CONF=`cygpath -p -w "$MC_CLIENT_CONF"`
    ;;
esac

launch_service()
{
    pidpath="$1"
    foreground="$2"
    props="$3"
    class="$4"
    mc_client_parms="-Dlogback.configurationFile=logback.xml"
    mc_client_parms="$mc_client_parms -Dmc-client.logdir=$MC_CLIENT_HOME/logs"

    if [ "x$pidpath" != "x" ]; then
        mc_client_parms="$mc_client_parms -Dmc-client-pidfile=$pidpath"
    fi

    # The mc-client-foreground option will tell CassandraDaemon not
    # to close stdout/stderr, but it's up to us not to background.
    if [ "x$foreground" != "x" ]; then
        mc_client_parms="$mc_client_parms -Dmc-client-foreground=yes"
        #exec "$JAVA" $JVM_OPTS $mc_client_parms -cp "$CLASSPATH" $props "$class"
        exec "$JAVA" $JVM_OPTS -jar $MC_CLIENT_HOME/lib/mc-client-0.3-SNAPSHOT.jar $MC_CLIENT_HOME
    # Startup CassandraDaemon, background it, and write the pid.
    else
        #exec "$JAVA" $JVM_OPTS $mc_client_parms -cp "$CLASSPATH" $props "$class" <&- &
        
        [ ! -z "$pidpath" ] && printf "%d" $! > "$pidpath"
        true
    fi

    return $?
}

# Parse any command line options.
args=`getopt vfhp:bD:H:E: "$@"`
eval set -- "$args"

classname="net.maritimecloud.internal.client.Main"

while true; do
    case "$1" in
        -p)
            pidfile="$2"
            shift 2
        ;;
        -f)
            foreground="yes"
            shift
        ;;
        -h)
            echo "Usage: $0 [-f] [-h] [-p pidfile] [-H dumpfile] [-E errorfile]"
            exit 0
        ;;
        -v)
            "$JAVA" -cp "$CLASSPATH" org.apache.mc-client.tools.GetVersion
            exit 0
        ;;
        -D)
            properties="$properties -D$2"
            shift 2
        ;;
        -H)
            properties="$properties -XX:HeapDumpPath=$2"
            shift 2
        ;;
        -E)
            properties="$properties -XX:ErrorFile=$2"
            shift 2
        ;;
        --)
            shift
            break
        ;;
        *)
            echo "Error parsing arguments!" >&2
            exit 1
        ;;
    esac
done

# Start up the service
launch_service "$pidfile" "$foreground" "$properties" "$classname"

exit $?

# vi:ai sw=4 ts=4 tw=0 et
