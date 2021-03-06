/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uberfire.security.impl.authz;

import org.uberfire.security.authz.AuthorizationResult;
import org.uberfire.security.authz.VotingAlgorithm;
import org.uberfire.security.authz.VotingStrategy;

import static org.uberfire.commons.validation.PortablePreconditions.checkNotNull;
import static org.uberfire.security.authz.AuthorizationResult.*;

/**
 * Algorithm implementation of the {@link VotingStrategy#CONSENSUS} strategy.
 */
//TODO: needs review. use a minimum consensus percentage?
public class ConsensusBasedVoter implements VotingAlgorithm {

    @Override
    public AuthorizationResult vote(final Iterable<AuthorizationResult> results) {
        checkNotNull("results", results);
        for (final AuthorizationResult currentResult : results) {
            if (currentResult.equals(ACCESS_DENIED)) {
                return ACCESS_DENIED;
            }
        }
        return ACCESS_GRANTED;
    }
}
