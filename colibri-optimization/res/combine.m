function [input] = combine(current_time, current_interval, time_inputs, future_steps, future_occupancy, future_influences, future_states, history_steps, history_occupancy, history_influences, history_states)
% This function combines the various input matrices to one combined matrix.
 
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
    disp('MATLAB: combine start');
    
    % ----------------------------------------------------------------
    
    % get row positions
    pos_time = 1;
    pos_occupancy = pos_time + time_inputs;
    pos_influences = pos_occupancy + size(future_occupancy,1);
    pos_states = pos_influences + size(future_influences, 1);
    pos_end = pos_states + size(future_states,1) - 1;
    
    % initialize matrix
    input = zeros(pos_end,(future_steps + history_steps));

    % ----------------------------------------------------------------
    
    % TODO test usage of cyclic encoded time
	
    % sinTime=sin(2*pi*(60*hour(timestamp) + minute(timestamp))/1440);
    % cosTime=cos(2*pi*(60*hour(timestamp) + minute(timestamp))/1440);
    
    % set time values for past slots
    timeslot = current_time - minutes(current_interval);

    for t=history_steps:-1:1
        input(1,t) = hour(timeslot) * 60 + minute(timeslot);
        input(2,t) = weekday(timeslot);
        input(3,t) = week(timeslot);
        input(4,t) = month(timeslot);
        input(5,t) = quarter(timeslot);
        timeslot = timeslot - minutes(current_interval);
    end;
    
    % set time values for future slots
    timeslot = current_time;

    for t=1:future_steps
        input(1,t + history_steps) = hour(timeslot) * 60 + minute(timeslot);
        input(2,t + history_steps) = weekday(timeslot);
        input(3,t + history_steps) = week(timeslot);
        input(4,t + history_steps) = month(timeslot);
        input(5,t + history_steps) = quarter(timeslot);
        timeslot = timeslot + minutes(current_interval);
    end;
    
    % ----------------------------------------------------------------

    % set historic values
    if (history_steps > 0)
        input(pos_occupancy:(pos_influences - 1),1:history_steps) = history_occupancy;
        input(pos_influences:(pos_states - 1),1:history_steps) = history_influences;
        input(pos_states:end,1:history_steps) = history_states;    
    end;
    
    % set future values
    if (future_steps > 0)
        input(pos_occupancy:(pos_influences - 1),(history_steps + 1):end) = future_occupancy;
        input(pos_influences:(pos_states - 1),(history_steps + 1):end) = future_influences;
        input(pos_states:end,(history_steps + 1):end) = future_states;
    end;
    
    % ----------------------------------------------------------------
    
    % log message
    disp('MATLAB: combine end');
    
end
