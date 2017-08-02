function [q, l, s] = split(x, data)
% This function splits the solution row vector into matrices.

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

    % no log message
    
    % ----------------------------------------------------------------

    % reshape solution to extract decision variables from x
    solution = reshape(x,[size(x,2)/data.future_steps,data.future_steps]);

    % first part of solution vector per time slot are portions of supply
    q = solution(1:data.suppliers,:);

    % seond part are consuming devices (storage devices are at the end)
    l = solution((data.suppliers+1):(data.suppliers + data.consumers),:);

    % third part are providing devices
    s = solution((data.suppliers+data.consumers+1):end,:); % s = ones(data.suppliers,data.future_steps);

    % ----------------------------------------------------------------

    % no log message
    
end

