/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.attribute.expression.language.evaluation.functions;


import org.apache.commons.codec.binary.Hex;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.nifi.attribute.expression.language.evaluation.*;
import org.apache.nifi.attribute.expression.language.exception.AttributeExpressionLanguageException;

public class HashEvaluator extends StringEvaluator {

    private final Evaluator<String> algorithm;
    private final Evaluator<String> subject;

    public HashEvaluator(final Evaluator<String> subject, final Evaluator<String> algorithm) {
        this.subject = subject;
        this.algorithm = algorithm;
    }

    @Override
    public QueryResult<String> evaluate(final Map<String, String> attributes, final EvaluatorState context) {
        final String subjectValue = subject.evaluate(attributes, context).getValue();
        if (subjectValue == null) {
            return new StringQueryResult(null);
        }

        final String algorithmValue = algorithm.evaluate(attributes,context).getValue();
        final MessageDigest digest = getDigest(algorithmValue);

        final byte[] dv = digest.digest(subjectValue.getBytes());
        return new StringQueryResult(Hex.encodeHexString(dv));
    }

    @Override
    public Evaluator<?> getSubjectEvaluator() {
        return subject;
    }

    private MessageDigest getDigest(String algorithm){
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new AttributeExpressionLanguageException("Invalid hash algorithm: " + algorithm, e);
        }
    }

}
