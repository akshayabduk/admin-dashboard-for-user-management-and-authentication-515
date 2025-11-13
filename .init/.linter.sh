#!/bin/bash
cd /home/kavia/workspace/code-generation/admin-dashboard-for-user-management-and-authentication-515/fido2_backend
./gradlew checkstyleMain
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
   exit 1
fi

