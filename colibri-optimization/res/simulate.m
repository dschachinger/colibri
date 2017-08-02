function [output] = simulate(steps, network, input, magnitude, initialtarget)
% This function simulates a neural network.

% **************************************************************************************************
% * Copyright (c) 2016, Automation Systems Group, Institute of Computer Aided Automation, TU Wien
% * All rights reserved.
% *
% * Redistribution and use in source and binary forms, with or without
% * modification, are permitted provided that the following conditions
% * are met:
% * 1. Redistributions of source code must retain the above copyright
% *    notice, this list of conditions and the following disclaimer.
% * 2. Redistributions in binary form must reproduce the above copyright
% *    notice, this list of conditions and the following disclaimer in the
% *    documentation and/or other materials provided with the distribution.
% * 3. Neither the name of the Institute nor the names of its contributors
% *    may be used to endorse or promote products derived from this software
% *    without specific prior written permission.
% *
% * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS "AS IS" AND
% * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
% * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
% * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
% * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
% * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
% * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
% * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
% * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
% * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
% * SUCH DAMAGE.
% *************************************************************************************************/

    % no log messages

    % ----------------------------------------------------------------

    % restrict input in accordance with input delays
    input = input(:,(1 + (size(initialtarget,2) - network.numInputDelays)) : end);

    % convert to sequential vector
    seq_input = con2seq(input);

    % network with feedback
    if network.numInputs >= 2

        % TODO closed loop used its own prediction (multi-step-ahead).
        % one-step-ahead with measured targets as inputs is much more accurate.
        % with [X,T] = simplenarx_dataset no difference between open and closed loop!?
        % maybe, the features are bad (time of day, ...)
        % closed-loop-problem: https://de.mathworks.com/help/nnet/examples/maglev-modeling.html
        % maglev_dataset has the same problems with closed loop
        
        %close the open feedback loop
        network = closeloop(network);

        % prepare and convert targets
        initialtarget = [ initialtarget NaN(1,steps) ];
        seq_targets = con2seq(initialtarget);

        % prepare input
        [Xs,Xi,Ai] = preparets(network,seq_input,{},seq_targets);

    else

        % prepare input (without targets)
        [Xs,Xi,Ai] = preparets(network,seq_input);

    end;

    % simulate network
    seq_output = sim(network,Xs,Xi,Ai);

    % convert and scale output
    output = cell2mat(seq2con(seq_output)) / magnitude;

    % ----------------------------------------------------------------

    % no log messages

end

    