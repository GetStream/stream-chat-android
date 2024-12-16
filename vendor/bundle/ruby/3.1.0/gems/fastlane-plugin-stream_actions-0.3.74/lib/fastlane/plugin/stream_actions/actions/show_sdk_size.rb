module Fastlane
  module Actions
    class ShowSdkSizeAction < Action
      def self.run(params)
        warning_status = '🟡' # Warning if a branch is #{max_tolerance} less performant than the benchmark
        fail_status = '🔴' # Failure if a branch is more than #{max_tolerance} less performant than the benchmark
        success_status = '🟢' # Success if a branch is more performant or equals to the benchmark
        outstanding_status = '🚀' # Outstanding performance

        metrics_dir = 'metrics'
        FileUtils.remove_dir(metrics_dir, force: true)
        sdk_size_path = "#{metrics_dir}/#{params[:github_repo].split('/').last}-size.json"
        sh("git clone git@github.com:GetStream/stream-internal-metrics.git #{metrics_dir}/")
        is_release = other_action.current_branch.include?('release/')
        benchmark_config = JSON.parse(File.read(sdk_size_path))
        benchmark_key = is_release ? 'release' : 'develop'
        benchmark_sizes = benchmark_config[benchmark_key]
        is_kb = params[:size_ext] == 'KB'

        table_header = '## SDK Size'
        markdown_table = "#{table_header}\n| `title` | `#{is_release ? 'previous release' : 'develop'}` | `#{is_release ? 'current release' : 'branch'}` | `diff` | `status` |\n| - | - | - | - | - |\n"
        params[:branch_sizes].each do |sdk_name, branch_value_kb|
          branch_value_mb = (branch_value_kb / 1024.0).round(2)
          branch_value = is_kb ? branch_value_kb.round(0) : branch_value_mb
          benchmark_value_kb = benchmark_sizes[sdk_name.to_s]
          benchmark_value_mb = (benchmark_value_kb / 1024.0).round(2)
          benchmark_value = is_kb ? benchmark_value_kb.round(0) : benchmark_value_mb
          max_tolerance = params[:max_tolerance] || is_kb ? 500 : 5000 # By default, Max Tolerance is 500 Kilobytes or 5000 Bytes
          fine_tolerance = params[:fine_tolerance] || is_kb ? 250 : 2500 # By default, Fine Tolerance is 250 Kilobytes or 2500 Bytes

          diff_kb = (branch_value_kb - benchmark_value_kb).round(0)
          diff_b = ((branch_value_kb - benchmark_value_kb) * 1024).round(0)
          diff = is_kb ? diff_b : diff_kb
          diff_ext = is_kb ? 'B' : 'KB'

          diff_sign = if diff.zero?
                        ''
                      elsif diff.positive?
                        '+'
                      else
                        '-'
                      end

          status_emoji = if diff < 0
                           outstanding_status
                         elsif diff >= max_tolerance
                           fail_status
                         elsif diff >= fine_tolerance
                           warning_status
                         else
                           success_status
                         end

          markdown_table << "|#{sdk_name}|#{benchmark_value} #{params[:size_ext]}|#{branch_value} #{params[:size_ext]}|#{diff_sign}#{diff.abs} #{diff_ext}|#{status_emoji}|\n"
        end

        FastlaneCore::PrintTable.print_values(title: 'Benchmark', config: benchmark_sizes)
        FastlaneCore::PrintTable.print_values(title: 'SDK Size', config: params[:branch_sizes])

        if other_action.is_ci
          if is_release || (ENV['GITHUB_EVENT_NAME'].to_s == 'push' && ["main", "develop"].include?(other_action.current_branch))
            benchmark_config[benchmark_key] = params[:branch_sizes]
            File.write(sdk_size_path, JSON.pretty_generate(benchmark_config))
            Dir.chdir(File.dirname(sdk_size_path)) do
              if sh('git status -s').to_s.empty?
                UI.important('No changes in SDK sizes benchmarks.')
              else
                sh('git add -A')
                sh("git commit -m 'Update #{sdk_size_path}'")
                sh('git push')
              end
            end
          end

          other_action.pr_comment(text: markdown_table, edit_last_comment_with_text: table_header)
        end

        UI.user_error!("#{table_header} benchmark failed.") if markdown_table.include?(fail_status)
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        'Show SDKs size'
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            env_name: 'GITHUB_REPOSITORY',
            key: :github_repo,
            description: 'GitHub repo name',
            verify_block: proc do |name|
              UI.user_error!("GITHUB_REPOSITORY should not be empty") if name.to_s.empty?
            end
          ),
          FastlaneCore::ConfigItem.new(
            key: :branch_sizes,
            description: 'Branch sizes',
            is_string: false,
            verify_block: proc do |s|
              UI.user_error!("Branch sizes have to be specified") if s.nil?
            end
          ),
          FastlaneCore::ConfigItem.new(
            key: :size_ext,
            description: 'SDK size extension (KB or MB)',
            default_value: 'MB'
          ),
          FastlaneCore::ConfigItem.new(
            key: :max_tolerance,
            description: 'Max tolerance (in KB `if size_ext == MB` or in B `if size_ext == KB`)',
            is_string: false,
            optional: true
          ),
          FastlaneCore::ConfigItem.new(
            key: :fine_tolerance,
            description: 'Fine tolerance (in KB `if size_ext == MB` or in B `if size_ext == KB`)',
            is_string: false,
            optional: true
          )
        ]
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
