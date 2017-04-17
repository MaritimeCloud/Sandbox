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


if [ "x$MC_CLIENT_HOME" = "x" ]; then
    MC_CLIENT_HOME="`dirname "$0"`/.."
fi

# The directory where configs live (required)
if [ "x$MC_CLIENT_CONF" = "x" ]; then
    MC_CLIENT_CONF="$MC_CLIENT_HOME/conf"
fi

# This can be the path to a jar file, or a directory containing the 
# compiled classes. NOTE: This isn't needed by the startup script,
# it's just used here in constructing the classpath.
#cassandra_bin="$MC_CLIENT_HOME/build/classes/main"
#cassandra_bin="$cassandra_home/build/cassandra.jar"

# the default location for commitlogs, sstables, and saved caches
# if not set in cassandra.yaml
mc_client_storagedir="$MC_CLIENT_HOME/data"

# JAVA_HOME can optionally be set here
#JAVA_HOME=/usr/local/jdk6

# The java classpath (required)
#CLASSPATH="$MC_CLIENT_CONF:$cassandra_bin"

#for jar in "$MC_CLIENT_HOME"/lib/*.jar; do
#    CLASSPATH="$CLASSPATH:$jar"
#done

