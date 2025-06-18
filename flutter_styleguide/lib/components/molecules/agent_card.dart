import 'package:flutter/material.dart';
import '../../models/agent.dart';
import '../../theme/app_colors.dart';
import '../atoms/status_dot.dart';
import '../atoms/tag_chip.dart';

class AgentCard extends StatelessWidget {
  final Agent agent;
  final VoidCallback? onTap;
  final VoidCallback? onEdit;
  final VoidCallback? onDelete;

  const AgentCard({
    Key? key,
    required this.agent,
    this.onTap,
    this.onEdit,
    this.onDelete,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(8),
        side: BorderSide(color: AppColors.border),
      ),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(8),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Avatar
                  Container(
                    width: 48,
                    height: 48,
                    decoration: BoxDecoration(
                      color: AppColors.primary.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: agent.avatarUrl != null
                        ? ClipRRect(
                            borderRadius: BorderRadius.circular(8),
                            child: Image.network(
                              agent.avatarUrl!,
                              fit: BoxFit.cover,
                              errorBuilder: (context, error, stackTrace) {
                                return Icon(
                                  Icons.person,
                                  color: AppColors.primary,
                                  size: 24,
                                );
                              },
                            ),
                          )
                        : Icon(
                            Icons.person,
                            color: AppColors.primary,
                            size: 24,
                          ),
                  ),
                  const SizedBox(width: 16),
                  // Name and description
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Expanded(
                              child: Text(
                                agent.name,
                                style: const TextStyle(
                                  fontSize: 16,
                                  fontWeight: FontWeight.w600,
                                  color: AppColors.textPrimary,
                                ),
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                              ),
                            ),
                            // Status indicator
                            Row(
                              children: [
                                StatusDot(
                                  status: _getStatusType(agent.status),
                                  size: 8,
                                ),
                                const SizedBox(width: 6),
                                Text(
                                  agent.status,
                                  style: TextStyle(
                                    fontSize: 12,
                                    color: AppColors.textSecondary,
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ),
                        const SizedBox(height: 4),
                        Text(
                          agent.description,
                          style: TextStyle(
                            fontSize: 14,
                            color: AppColors.textSecondary,
                          ),
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              if (agent.tags.isNotEmpty) ...[
                const SizedBox(height: 12),
                Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: agent.tags
                      .map((tag) => TagChip(label: tag))
                      .toList(),
                ),
              ],
              const SizedBox(height: 12),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'Created: ${_formatDate(agent.createdAt)}',
                    style: TextStyle(
                      fontSize: 12,
                      color: AppColors.textSecondary,
                    ),
                  ),
                  if (agent.lastActive != null)
                    Text(
                      'Last active: ${_formatDate(agent.lastActive!)}',
                      style: TextStyle(
                        fontSize: 12,
                        color: AppColors.textSecondary,
                      ),
                    ),
                ],
              ),
              if (onEdit != null || onDelete != null) ...[
                const SizedBox(height: 12),
                Row(
                  mainAxisAlignment: MainAxisAlignment.end,
                  children: [
                    if (onEdit != null)
                      IconButton(
                        onPressed: onEdit,
                        icon: Icon(
                          Icons.edit,
                          size: 20,
                          color: AppColors.primary,
                        ),
                        tooltip: 'Edit',
                        constraints: const BoxConstraints(),
                        padding: const EdgeInsets.all(8),
                      ),
                    if (onDelete != null)
                      IconButton(
                        onPressed: onDelete,
                        icon: Icon(
                          Icons.delete,
                          size: 20,
                          color: AppColors.error,
                        ),
                        tooltip: 'Delete',
                        constraints: const BoxConstraints(),
                        padding: const EdgeInsets.all(8),
                      ),
                  ],
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }

  StatusType _getStatusType(String status) {
    switch (status.toLowerCase()) {
      case 'active':
      case 'online':
        return StatusType.success;
      case 'inactive':
      case 'offline':
        return StatusType.neutral;
      case 'warning':
      case 'pending':
        return StatusType.warning;
      case 'error':
      case 'failed':
        return StatusType.error;
      default:
        return StatusType.info;
    }
  }

  String _formatDate(DateTime date) {
    return '${date.day}/${date.month}/${date.year}';
  }
} 