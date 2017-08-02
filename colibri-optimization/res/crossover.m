function [xoverKids] = crossover(parents,options,nvars,~,~,thisPopulation,ConstrFcn)
% This is the crossover function for the genetic algorithm.

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
    disp('MATLAB: crossover start');
    
    % ----------------------------------------------------------------
    
    % define globals
    global data;
    global constraints;
    
    % ----------------------------------------------------------------
    
    % define block size
    width = nvars / data.future_steps;
                
    % initialize kids
    xoverKids = NaN(size(parents,2)/2,nvars);
    
    % create kids with 2-point crossover
    for i=1:size(parents,2)/2
    
        % initialize stopping counter
        valCount = 0;
        
        % validity indicator
        valid = false;
        
        % re-run crossover until solution is feasible
        while ~valid
            
            % exit loop if solution does not get valid                
            if valCount >= data.validity_count                
                break;
            end;
            
            % set two unique random points
            point1 = randi(data.future_steps);
            point2 = point1;
            
            while point2 == point1
                point2 = randi(data.future_steps);
            end;
            
            if point2 < point1
                temp = point2;
                point2 = point1;
                point1 = temp;
            end;
            
            % adopt points to block size
            point1 = point1 * width;
            point2 = point2 * width;
            
            % get parents from selection
            parent1 = thisPopulation(parents((i-1)*2 + 1),:);
            parent2 = thisPopulation(parents((i-1)*2 + 2),:);
            
            % recombine the parents
            xoverKids(i,:) = [ parent1(1,1:point1) parent2(1,point1+1:point2) parent1(point2+1:end) ];
            
            % validate the parents
            valid=validate(xoverKids(i,:), constraints.linear_A, constraints.linear_b, constraints.linear_Aeq, constraints.linear_beq, constraints.lower_bounds, constraints.upper_bounds, ConstrFcn, options.TolCon);            
            
            % increment counter
            valCount = valCount + 1;
        end;
    end;
    
    % ----------------------------------------------------------------
    
    % no log message
    
end

