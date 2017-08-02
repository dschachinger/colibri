function [node] = design(default_type, default_features, data, input, output)
% This function creates and trains a particular neural network.

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
    disp('MATLAB: design start');
    
    % ----------------------------------------------------------------

    % configuration parameters    
    params.direction = false; % true = up, false = down
    params.level = 1;
    params.level_first = true;
    params.stop = false;
    params.noimprove = 0;
    params.noimprove_max = data.train_noimprove_default;
    
    params.hidden_train = data.train_hiddensize;
    params.hidden_temp = double(data.hidden_size);
    params.hidden_initial = double(data.hidden_size);
    params.hidden_best = double(data.hidden_size);
    
    params.features_train = data.train_features;
    params.features_initial = default_features;
    params.features_best = default_features;
    params.features_temp = default_features;
    params.features_skip = [ data.input_occupancy:(data.input_influences-1) data.input_states:data.input_end ];
    
    params.magnitude_train = data.train_magnitude;
    params.magnitude_initial = 1.0;
    params.magnitude_best = 1.0;
    params.magnitude_temp = 1.0;
    
    params.performance_train = data.train_performancefct;
    params.performance_initial = 'mse';
    params.performance_next = 'sse';
    params.performance_temp = 'mse';
    
    params.division_train = data.train_division;
    params.division_initial = data.train_division_default;
    params.division_best = data.train_division_default;
    params.division_temp = data.train_division_default;

    params.type_train = data.train_type;
    params.type_initial = default_type;
    params.type_best = default_type;
    params.type_temp = default_type;    
    
    % iteration count
    improvement_iteration = 1;
        
    % initialize assessment variables
    best_net = NaN;
    best_perf = realmax('double');
    last_perf = realmax('double');
    
    % ----------------------------------------------------------------
    
    % improvement loop
    while ~params.stop
        
        % start time
        tic

        % log message
        fprintf('MATLAB: design (start improvement, iteration=%d)\n', improvement_iteration);
        fprintf('MATLAB: design (type=%d, division=%d, hidden=%d, perf=%s, magnitude=%d)\n', params.type_temp, params.division_temp(1), params.hidden_temp(1), params.performance_temp, params.magnitude_temp(1));
		fprintf('MATLAB: design (features=');
		fprintf('%d',params.features_temp);
        fprintf(')\n');
        
        % resets the randomizer to a default value for reproducibility
        rng('default')

        % create feature vector
        features = zeros(1,sum(params.features_temp));
        temp = 1;
        for p=1:size(params.features_temp,2)
            if params.features_temp(1,p) == 1
                features(1,temp) = p;
                temp = temp+1;
            end;
        end;
                
        % create neural network
        if params.type_temp == 1
            
            % networks that do not depend on any past values
            temp_net = feedforwardnet(double(params.hidden_temp));
            
        elseif params.type_temp == 2
            
            % networks that depend not only on one distinct input but on a series
            temp_net = timedelaynet(1:double(data.history_steps),double(params.hidden_temp));
            
        elseif params.type_temp == 3
            
            % networks that also depend on the progress of changed outputs
            temp_net = narxnet(1:double(data.history_steps),1:double(data.history_steps),double(params.hidden_temp));
            
        end;
        
        % set performance function
        temp_net.performFcn = params.performance_temp;        
        temp_net.trainFcn = data.train_algorithm;
        
        % disable training window
        temp_net.trainParam.showWindow = false;
                        
        % create sequential vectors (input, output/magnitude)
        seq_input = con2seq(input(features,:));
        seq_output = con2seq(output * params.magnitude_temp);
        
        % split input and output
        splitter = floor(size(seq_input, 2) * params.division_temp);
        seq_input_train = seq_input(:, 1:splitter);
        seq_output_train = seq_output(:, 1:splitter);
        
        % prepare time series (recurrent with feedback targets)
        if params.type_temp == 3
            [Xs1,Xi1,Ai1,Ts1] = preparets(temp_net,seq_input_train,{},seq_output_train);
            [Xs2,Xi2,Ai2,Ts2] = preparets(temp_net,seq_input,{},seq_output);
        else
            [Xs1,Xi1,Ai1,Ts1] = preparets(temp_net,seq_input_train,seq_output_train);
            [Xs2,Xi2,Ai2,Ts2] = preparets(temp_net,seq_input,seq_output);
        end;
                
        % run training iterations
        for i = 1:data.train_iterations
            
            % log message
            fprintf('MATLAB: design (start training, iteration=%d)\n', i);
            
            % run training
            temp_net_loop = train(temp_net,Xs1,Ts1,Xi1,Ai1);
            
            % run network
            Y2 = sim(temp_net_loop,Xs2,Xi2,Ai2);
            
            % get performance (scaled by magnitude)
            perf = perform(temp_net_loop,Y2,Ts2) / params.magnitude_temp;
            
            % set best values
            if (perf <= best_perf)
                best_perf = perf;
                best_net = temp_net_loop;
            end;
            
            % log message
            fprintf('MATLAB: design (end training, performance=%d)\n', perf);
        end;
        
        % get new parameter configuration        
        params = improve(params, (last_perf-best_perf) > 0);
        
        % increase iteration counter
        improvement_iteration = improvement_iteration + 1;
        
        % set latest best performance
        last_perf = best_perf;

        % stop time
        elapsed_time = toc;
        fprintf('MATLAB: design (end improvement, time=%f seconds)\n', elapsed_time);
    end;
        
    % ----------------------------------------------------------------
    
    % set best network
    node.net = best_net;
    
    % set best features
    node.features = params.features_best;
    
    % set best magnitude
    node.magnitude = params.magnitude_best;
    
    % ----------------------------------------------------------------
    
    % log message
    disp('MATLAB: design end');
    
end
