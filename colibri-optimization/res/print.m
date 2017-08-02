function [result] = print(node,input,target)
% This function plots the network performance.

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

	% log message
    disp('MATLAB: print start');
    
    % ----------------------------------------------------------------
	
	% print values	
	fprintf('magnitude=%d\n',node.magnitude);
	fprintf('features=');	
    fprintf('%d',node.features);
	fprintf('\n');

    % init simulation
    steps = size(input,2) - node.net.numInputDelays;    
    initialtarget = [];
    
    if node.net.numInputDelays > 0
        initialtarget = target(:,1:node.net.numInputDelays);
        target = target(:,(1 + size(initialtarget,2)) : end);
    end;
    
    % simulate network
    output = simulate(steps, node.net, input, node.magnitude, initialtarget);
	
    % print performance
    fprintf('performance=%d\n',perform(node.net,output,target));	
    
	% plot result    
    output = con2seq(output);
    target = con2seq(target);    
    plotresponse(output,target);
    
    % view network
	view(node.net);
    
    % ----------------------------------------------------------------
	
	% set dummy result
    result = 1;
	
	% log message
    disp('MATLAB: print start');    

end

