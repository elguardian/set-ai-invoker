/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.set.agent.invoker.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubEventPayload {

    private String action;
    private int number;

    @JsonProperty("pull_request")
    private GitHubPullRequest pullRequest;

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public GitHubPullRequest getPullRequest() { return pullRequest; }
    public void setPullRequest(GitHubPullRequest pullRequest) { this.pullRequest = pullRequest; }
}
