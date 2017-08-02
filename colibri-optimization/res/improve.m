function [params] = improve(params, improvement)
% This function improves the neural network configuration.

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
    disp('MATLAB: improve start');
        
    % ----------------------------------------------------------------

    % overall strategy:
    %  
    %  0) important precondition: appropriate default setting (average)
    %
    %  1) use moderate setting (hidden size, division) to test feature dependency
    %  2) vary division of training data
    %  3) vary influence of hidden size
    %  4) test 'sse' instead of 'mse'
    %  1) finally, test different magnitudes for output/target data

    % ----------------------------------------------------------------

    if params.level == 1

        % improvement of feature selection is enabled
        if params.type_train == 1
        
            % set new best
            if improvement == true
                params.type_best = params.type_temp;
            end;

            params.type_temp = params.type_temp + 1;

            if params.type_temp > 3
                params.type_temp = params.type_best;
                params.direction = false;
                params.level = params.level + 1;
                params.level_first = true;
            end;

        % directly continue with next level
        else
            params.direction = false;
            params.level = params.level + 1;
            params.level_first = true;
        end;
    end;

    % ----------------------------------------------------------------

    if params.level == 2
        
        % improvement of feature selection is enabled
        if params.features_train == 1
            
            % set new best
            if improvement == true
                params.features_best = params.features_temp;
                params.noimprove = 0; 
            else
                params.noimprove = params.noimprove + 1;
            end;

            % reduce number of features
            if params.direction == false
                
                % initialize cursor or reset feature
                if params.level_first == true
                    params.features_selection = 0;
                    params.level_first = false;
                    params.noimprove = 0;
                elseif improvement == false
                    params.features_temp(params.features_selection) = 1;                    
                end;

                % increase feature pointer
                params.features_selection = params.features_selection + 1;
                
                % change indicator
                change = false;
                
                % find feature that can be removed
                for i = params.features_selection:size(params.features_temp,2)
                    
                    % remove feature
                    if params.features_temp(i) == 1
                        params.features_selection = i;
                        params.features_temp(i) = 0;
                        change = true;
                        break;
                    end;                    
                end;
                
                % no change was made (continue with more features)
                if change == false
                    params.features_temp = params.features_initial;                    
                    params.direction = true;
                    params.level_first = true;
                end;
            end;
            
            % increase number of features
            if params.direction == true
                
                % initialize cursor or reset feature
                if params.level_first == true
                    params.features_selection = 0;
                    params.level_first = false;
                    params.noimprove = 0;
                elseif improvement == false
                    params.features_temp(params.features_selection) = 0;                    
                end;

                % increase feature pointer
                params.features_selection = params.features_selection + 1;
                
                % change indicator
                change = false;
                
                % find feature that can be selected
                for i = params.features_selection:size(params.features_temp,2)
                    
                    % skip occupancy and state features for additional selection
                    if ~ismember([ i i ],params.features_skip)
                    
                        % select feature
                        if params.features_temp(i) == 0
                            params.features_selection = i;
                            params.features_temp(i) = 1;
                            change = true;
                            break;
                        end;
                    end;
                end;
                
                % no change was made (continue with more features)
                if change == false
                    params.features_temp = params.features_best;                    
                    params.direction = false;
                    params.level_first = true;
                    params.level = params.level + 1;
                end;                 
            end;
            
        % directly continue with next level
        else
            params.direction = false;
            params.level = params.level + 1;
            params.level_first = true;
        end;
    end;
    
    % ----------------------------------------------------------------

    if params.level == 3
        
        % improvement of division is enabled
        if params.division_train  == 1
            
			% TODO improve this level
			
            % not yet implemented: divideind, divideint, dividerand            
            % here, the window of testing is increased/reduced
            % separate test of performance is done for all data
            
            % set new best
            if improvement == true
                params.division_best = params.division_temp;
                params.noimprove = 0; 
            else
                params.noimprove = params.noimprove + 1;
            end;
            
            % reduce percentage
            if params.direction == false
                
                % initialize level
                if improvement == true || params.level_first == true || params.noimprove <= params.noimprove_max    
              
                    % reset no improve counter
                    if params.level_first == true
                        params.noimprove = 0; 
                    end;            

                    params.division_temp = params.division_temp - 0.1;
                    params.level_first = false;  
                else
                    params.division_temp = params.division_initial;
                    params.direction = true;
                    params.level_first = true;
                end;                
                
                % terminate this direction
                if params.division_temp < 0.1
                    params.division_temp = params.division_initial;
                    params.direction = true;
                    params.level_first = true;
                end;          
            end;
            
            % increase percentage
            if params.direction == true
                
                % there was an improvement
                if improvement == true || params.level_first == true || params.noimprove <= params.noimprove_max    
              
                    % reset no improve counter
                    if params.level_first == true
                        params.noimprove = 0; 
                    end;            

                    params.division_temp = params.division_temp + 0.1;
                    params.level_first = false;
                 else
                    params.division_temp = params.division_best;
                    params.direction = false;
                    params.level = params.level + 1;
                    params.level_first = true;
                end;   
                
                % terminate this direction (full input = 1 season => 70% training is enough)
                if params.division_temp >= 0.8
                    params.division_temp = params.division_best;                    
                    params.direction = false;
                    params.level_first = true;
                    params.level = params.level + 1;
                end; 
            end;
            
        % directly continue with next level
        else
            params.direction = false;
            params.level = params.level + 1;
            params.level_first = true;
        end;
    end;
    
    % ----------------------------------------------------------------
    
    if params.level == 4
             
         % improvement of hidden size is enabled
        if params.hidden_train == 1

            % set new best
            if improvement == true
                params.hidden_best = params.hidden_temp;
                params.noimprove = 0; 
            else
                params.noimprove = params.noimprove + 1;
            end;
            
            % decrease hidden size
            if params.direction == false
                
                % there was an improvement
                if improvement == true || params.level_first == true || params.noimprove <= params.noimprove_max    
              
                    % reset no improve counter
                    if params.level_first == true
                        params.noimprove = 0; 
                    end;            

                    params.hidden_temp = params.hidden_temp - 1;
                    params.level_first = false;                                    
                else
                    params.hidden_temp = params.hidden_initial;
                    params.direction = true;
                    params.level_first = true;
                end;                
                
                % terminate this direction
                if params.hidden_temp <= 0 
                    params.hidden_temp = params.hidden_initial;
                    params.direction = true;
                    params.level_first = true;
                end;         
            end;
                
            % increase hidden size
            if params.direction == true
                
                % there was an improvement
                if improvement == true || params.level_first == true || params.noimprove <= params.noimprove_max    
              
                    % reset no improve counter
                    if params.level_first == true
                        params.noimprove = 0; 
                    end;            

                    params.hidden_temp = params.hidden_temp + 1;
                    params.level_first = false;   
                else
                    params.hidden_temp = params.hidden_best;
                    params.direction = false;
                    params.level = params.level + 1;
                    params.level_first = true;
                end;    
                
                % terminate this direction
                if params.hidden_temp >= 5 * params.hidden_initial
                    params.hidden_temp = params.hidden_best;                    
                    params.direction = false;
                    params.level_first = true;
                    params.level = params.level + 1;
                end; 
            end;
            
        % directly continue with next level
        else
            params.direction = false;
            params.level = params.level + 1;
            params.level_first = true;
        end;
    end;
    
    % ----------------------------------------------------------------

    if params.level == 5
        
        % improvement of performance function is enabled
        if params.performance_train == 1

            % initialize cursor
            if params.level_first == true
                params.level_first = false;
                params.performance_temp = params.performance_next;
            else
                % no improvement with 'sse' (reset to 'mse')
                if improvement == false
                    params.performance_temp = params.performance_initial;
                end;
                
                % go to next level
                params.direction = false;
                params.level = params.level + 1;
                params.level_first = true;
            end;
   
        % directly continue with next level
        else
            params.direction = false;
            params.level = params.level + 1;
            params.level_first = true;
        end;
    end;

    % ----------------------------------------------------------------

    if params.level == 6

        % improvement of magnitude is enabled
        if params.magnitude_train == 1
        
            % set new best
            if improvement == true
                params.magnitude_best = params.magnitude_temp;
                params.noimprove = 0; 
            else
                params.noimprove = params.noimprove + 1;
            end;
            
            % reduce magnitude
            if params.direction == false
                
                % there was an improvement
                if improvement == true || params.level_first == true || params.noimprove <= params.noimprove_max    
              
                    % reset no improve counter
                    if params.level_first == true
                        params.noimprove = 0; 
                    end;            

                    params.magnitude_temp = params.magnitude_temp / 10;
                    params.level_first = false;
                else
                    params.magnitude_temp = params.magnitude_initial;
                    params.direction = true;
                    params.level_first = true;
                end;                
                
                % terminate this direction
                if params.magnitude_temp <= 0.0001 
                    params.magnitude_temp = params.magnitude_initial;
                    params.direction = true;
                    params.level_first = true;
                end; 
            end;
            
            % increase 
            if params.direction == true
                
                % there was an improvement
                if improvement == true || params.level_first == true || params.noimprove <= params.noimprove_max    
              
                    % reset no improve counter
                    if params.level_first == true
                        params.noimprove = 0; 
                    end;            

                    params.magnitude_temp = params.magnitude_temp * 10;
                    params.level_first = false;
                else
                    params.magnitude_temp = params.magnitude_best;                    
                    params.direction = false;
                    params.level = params.level + 1;
                    params.level_first = true;
                end;
                
                % terminate this direction
                if params.magnitude_temp >= 10000 
                    params.magnitude_temp = params.magnitude_best;
                    params.direction = false;
                    params.level = params.level + 1;
                    params.level_first = true;
                end; 
            end;
                    
        % directly continue with next level
        else
            params.direction = false;
            params.level = params.level + 1;
            params.level_first = true;
        end;
    end;
    
    % ----------------------------------------------------------------

    % stop improvement
    if params.level > 6
        params.stop = true;
    end;
    
    % ----------------------------------------------------------------

    % log message
    disp('MATLAB: improve end');

end

