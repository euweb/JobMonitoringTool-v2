import React from 'react'
import {
  Box,
  Typography,
  Card,
  CardContent,
  Button,
} from '@mui/material'
import { AdminPanelSettings as AdminIcon } from '@mui/icons-material'

const AdminPanel: React.FC = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <AdminIcon />
        Admin Panel
      </Typography>
      
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            User Management
          </Typography>
          <Typography variant="body2" color="textSecondary" paragraph>
            Manage system users, roles, and permissions.
          </Typography>
          <Button variant="contained" color="primary">
            Manage Users
          </Button>
        </CardContent>
      </Card>
    </Box>
  )
}

export default AdminPanel