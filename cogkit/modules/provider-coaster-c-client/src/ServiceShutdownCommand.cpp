/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 *
 * Copyright 2014 University of Chicago
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * ServiceShutdownCommand.cpp
 *
 *  Created on: Sep 06, 2014
 *      Author: Mihael Hategan
 */

#include "ServiceShutdownCommand.h"
#include "Logger.h"
#include <sstream>

using namespace Coaster;

using std::string;

string ServiceShutdownCommand::NAME("SHUTDOWNSERVICE");

ServiceShutdownCommand::ServiceShutdownCommand(): Command(&NAME) {
}

ServiceShutdownCommand::~ServiceShutdownCommand() {
}

void ServiceShutdownCommand::send(CoasterChannel* channel, CommandCallback* cb) {
	Command::send(channel, cb);
}

void ServiceShutdownCommand::dataSent(Buffer* buf) {
	delete buf;
}

void ServiceShutdownCommand::replyReceived() {
	Command::replyReceived();
}
